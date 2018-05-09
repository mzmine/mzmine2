/* 
 * Copyright (C) 2016 Du-Lab Team <dulab.binf@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sf.mzmine.modules.peaklistmethods.peakpicking.adap3decompositionV2;

import com.google.common.collect.Range;
import dulab.adap.datamodel.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import dulab.adap.workflow.decomposition.Decomposition;
import dulab.adap.workflow.decomposition.RetTimeClusterer;
import net.sf.mzmine.datamodel.*;
import net.sf.mzmine.datamodel.impl.*;
import net.sf.mzmine.modules.peaklistmethods.qualityparameters.QualityParameters;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.taskcontrol.AbstractTask;
import net.sf.mzmine.taskcontrol.TaskStatus;

import javax.annotation.Nonnull;

/**
 *
 * @author aleksandrsmirnov
 */
public class ADAP3DecompositionV2Task extends AbstractTask {

    // Logger.
    private static final Logger LOG = Logger.getLogger(ADAP3DecompositionV2Task.class.getName());
    
    // Peak lists.
    private final MZmineProject project;
    private final ChromatogramPeakPair originalLists;
    private PeakList newPeakList;
    private final Decomposition decomposition;
    
    // User parameters
    private final ParameterSet parameters;
    
    ADAP3DecompositionV2Task(final MZmineProject project, final ChromatogramPeakPair lists,
                             final ParameterSet parameterSet)
    {
        // Initialize.
        this.project = project;
        parameters = parameterSet;
        originalLists = lists;
        newPeakList = null;
        decomposition = new Decomposition();
    }
    
    @Override
    public String getTaskDescription() {
        return "ADAP Peak decomposition on " + originalLists;
    }
    
    @Override
    public double getFinishedPercentage() {
        return decomposition.getProcessedPercent();
    }
    
    @Override
    public void run() {
        if (!isCanceled()) {
            String errorMsg = null;

            setStatus(TaskStatus.PROCESSING);
            LOG.info("Started ADAP Peak Decomposition on " + originalLists);

            // Check raw data files.
            if (originalLists.chromatograms.getNumberOfRawDataFiles() > 1
                    && originalLists.peaks.getNumberOfRawDataFiles() > 1)
            {
                setStatus(TaskStatus.ERROR);
                setErrorMessage("Peak Decomposition can only be performed on peak lists with a single raw data file");
            } else {
                
                try {
                    
                    newPeakList = decomposePeaks(originalLists);
                    
                    if (!isCanceled()) {

                        // Add new peaklist to the project.
                        project.addPeakList(newPeakList);

                        // Add quality parameters to peaks
                        QualityParameters.calculateQualityParameters(newPeakList);

                        // Remove the original peaklist if requested.
                        if (parameters.getParameter(ADAP3DecompositionV2Parameters.AUTO_REMOVE).getValue()) {
                            project.removePeakList(originalLists.chromatograms);
                            project.removePeakList(originalLists.peaks);
                        }

                        setStatus(TaskStatus.FINISHED);
                        LOG.info("Finished peak decomposition on " + originalLists);
                    }
                    
                } catch (IllegalArgumentException e) {
                    errorMsg = "Incorrect Peak List selected:\n"
                            + e.getMessage();
                } catch (IllegalStateException e) {
                    errorMsg = "Peak decompostion error:\n"
                            + e.getMessage();
                } catch (Exception e) {
                    errorMsg = "'Unknown error' during peak decomposition. \n"
                            + e.getMessage();
                } catch (Throwable t) {

                    setStatus(TaskStatus.ERROR);
                    setErrorMessage(t.getMessage());
                    LOG.log(Level.SEVERE, "Peak decompostion error", t);
                }

                // Report error.
                if (errorMsg != null) {
                    setErrorMessage(errorMsg);
                    setStatus(TaskStatus.ERROR);
                }
            }
        }
    }
    
    private PeakList decomposePeaks(@Nonnull ChromatogramPeakPair lists)
    {
        RawDataFile dataFile = lists.chromatograms.getRawDataFile(0);
        
        // Create new peak list.
        final PeakList resolvedPeakList = new SimplePeakList(lists.peaks + " "
                + parameters.getParameter(ADAP3DecompositionV2Parameters.SUFFIX).getValue(), dataFile);
        
        // Load previous applied methods.
        for (final PeakList.PeakListAppliedMethod method : lists.peaks.getAppliedMethods()) {
            resolvedPeakList.addDescriptionOfAppliedTask(method);
        }

        // Add task description to peak list.
        resolvedPeakList.addDescriptionOfAppliedTask(new SimplePeakListAppliedMethod(
                        "Peak deconvolution by ADAP-3", parameters));
        
        // Collect peak information
        List<BetterPeak> chromatograms = new ADAP3DecompositionV2Utils().getPeaks(lists.chromatograms);
        RetTimeClusterer.Item[] ranges = Arrays.stream(lists.peaks.getRows())
                .map(PeakListRow::getBestPeak)
                .map(p -> new RetTimeClusterer.Item(p.getRT(), p.getRawDataPointsRTRange(), p.getMZ()))
                .toArray(RetTimeClusterer.Item[]::new);

        // Find components (a.k.a. clusters of peaks with fragmentation spectra)
        List<BetterComponent> components = getComponents(chromatograms, ranges);

        // Create PeakListRow for each components
        List <PeakListRow> newPeakListRows = new ArrayList <> ();

        int rowID = 0;

        for (final BetterComponent component : components)
        {
            if (component.spectrum.length == 0) continue;

            // Create a reference peal
            Feature refPeak = getFeature(dataFile, component);

            // Add spectrum
            List<DataPoint> dataPoints = new ArrayList <> ();
            for (int i = 0; i < component.spectrum.length; ++i) {
                double mz = component.spectrum.getMZ(i);
                double intensity = component.spectrum.getIntensity(i);
                if (intensity > 1e-3 * component.getIntensity())
                    dataPoints.add(new SimpleDataPoint(mz, intensity));
            }

            if (dataPoints.size() < 5) continue;

            refPeak.setIsotopePattern(new SimpleIsotopePattern(
                    dataPoints.toArray(new DataPoint[dataPoints.size()]),
                    IsotopePattern.IsotopePatternStatus.PREDICTED,
                    "Spectrum"));

            PeakListRow row = new SimplePeakListRow(++rowID);

            row.addPeak(dataFile, refPeak);

            // Set row properties
            row.setAverageMZ(refPeak.getMZ());
            row.setAverageRT(refPeak.getRT());
             
            // resolvedPeakList.addRow(row);
            newPeakListRows.add(row);
        }
        
        // ------------------------------------
        // Sort new peak rows by retention time
        // ------------------------------------
        
        newPeakListRows.sort(Comparator.comparingDouble(PeakListRow::getAverageRT));
        
        for (PeakListRow row : newPeakListRows)
            resolvedPeakList.addRow(row);
        
        return resolvedPeakList;
    }
    

    
    /**
     * Performs ADAP Peak Decomposition
     * 
     * @param chromatograms list of {@link BetterPeak} representing chromatograms
     * @param ranges arrays of {@link RetTimeClusterer.Item} containing ranges of detected peaks
     * @return Collection of dulab.adap.Component objects
     */
    
    private List<BetterComponent> getComponents(List<BetterPeak> chromatograms, RetTimeClusterer.Item[] ranges)
    {
        // -----------------------------
        // ADAP Decomposition Parameters
        // -----------------------------

        Decomposition.Parameters params = new Decomposition.Parameters();

        params.prefWindowWidth = parameters.getParameter(ADAP3DecompositionV2Parameters.PREF_WINDOW_WIDTH).getValue();
        params.minClusterSize = parameters.getParameter(ADAP3DecompositionV2Parameters.MIN_NUM_PEAK).getValue();
        params.retTimeTolerance = parameters.getParameter(ADAP3DecompositionV2Parameters.RET_TIME_TOLERANCE).getValue();
        params.smoothing = parameters.getParameter(ADAP3DecompositionV2Parameters.SMOOTHING).getValue();
        params.unimodality = parameters.getParameter(ADAP3DecompositionV2Parameters.UNIMODALITY).getValue();

        return decomposition.run(params, chromatograms, ranges);
    }

    @Nonnull
    private Feature getFeature(@Nonnull RawDataFile file, @Nonnull BetterPeak peak)
    {
        Chromatogram chromatogram = peak.chromatogram;

        // Retrieve scan numbers
        int representativeScan = 0;
        int[] scanNumbers = new int[chromatogram.length];
        int count = 0;
        for (int num : file.getScanNumbers())
        {
            double retTime = file.getScan(num).getRetentionTime();
            if (chromatogram.contains(retTime))
                scanNumbers[count++] = num;
            if (retTime == peak.getRetTime())
                representativeScan = num;
        }

        // Calculate peak area
        double area = 0.0;
        for (int i = 1; i < chromatogram.length; ++i) {
            double base = chromatogram.xs[i] - chromatogram.xs[i - 1];
            double height = 0.5 * (chromatogram.ys[i] + chromatogram.ys[i - 1]);
            area += base * height;
        }

        // Create array of DataPoints
        DataPoint[] dataPoints = new DataPoint[chromatogram.length];
        count = 0;
        for (double intensity : chromatogram.ys)
            dataPoints[count++] = new SimpleDataPoint(peak.getMZ(), intensity);

        return new SimpleFeature(file, peak.getMZ(), peak.getRetTime(), peak.getIntensity(),
                area, scanNumbers, dataPoints,
                Feature.FeatureStatus.MANUAL, representativeScan, representativeScan,
                Range.closed(peak.getFirstRetTime(), peak.getLastRetTime()),
                Range.closed(peak.getMZ() - 0.01, peak.getMZ() + 0.01),
                Range.closed(0.0, peak.getIntensity()));
    }

    @Override
    public void cancel() {
        decomposition.cancel();
        super.cancel();
    }
}
