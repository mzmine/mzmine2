/*
 * Copyright 2006-2015 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.project.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Nonnull;
import javax.swing.SwingUtilities;

import net.sf.mzmine.datamodel.DataPoint;
import net.sf.mzmine.datamodel.MassList;
import net.sf.mzmine.datamodel.MassSpectrumType;
import net.sf.mzmine.datamodel.PolarityType;
import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.datamodel.Scan;
import net.sf.mzmine.desktop.impl.projecttree.RawDataTreeModel;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.masslistmethods.listexport.ListExportTask;
import net.sf.mzmine.util.ScanUtils;

import org.apache.commons.io.FilenameUtils;

import com.veritomyx.FileChecksum;
import com.google.common.collect.Range;

/**
 * Implementation of the Scan interface which stores raw data points in a
 * temporary file, accessed by RawDataFileImpl.readFromFloatBufferFile()
 */
public class StorableScan implements Scan {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private int scanNumber, msLevel, fragmentScans[];
    private double precursorMZ;
    private int precursorCharge;
    private double retentionTime;
    private Range<Double> mzRange;
    private DataPoint basePeak;
    private Double totalIonCurrent;
    private MassSpectrumType spectrumType;
    private int numberOfDataPoints;
    private RawDataFileImpl rawDataFile;
    private ArrayList<MassList> massLists = new ArrayList<MassList>();
    private PolarityType polarity;
    private String scanDefinition;
    private Range<Double> scanMZRange;
	private boolean centroided;

    private int storageID;

    /**
     * Constructor for creating a storable scan from a given scan
     */
    public StorableScan(Scan originalScan, RawDataFileImpl rawDataFile,
	    int numberOfDataPoints, int storageID) {

	// save scan data
	this.rawDataFile = rawDataFile;
	this.numberOfDataPoints = numberOfDataPoints;
	this.storageID = storageID;

	this.scanNumber = originalScan.getScanNumber();
	this.msLevel = originalScan.getMSLevel();
	this.retentionTime = originalScan.getRetentionTime();
	this.precursorMZ = originalScan.getPrecursorMZ();
	this.precursorCharge = originalScan.getPrecursorCharge();
	this.fragmentScans = originalScan.getFragmentScanNumbers();
	this.spectrumType = originalScan.getSpectrumType();
	this.mzRange = originalScan.getDataPointMZRange();
	this.basePeak = originalScan.getHighestDataPoint();
	this.totalIonCurrent = originalScan.getTIC();
	this.polarity = originalScan.getPolarity();
	this.scanDefinition = originalScan.getScanDefinition();
	this.scanMZRange = originalScan.getScanningMZRange();
	this.basePeak = originalScan.getBasePeak();
	this.centroided = originalScan.isCentroided();

    }

    public StorableScan(RawDataFileImpl rawDataFile, int storageID,
	    int numberOfDataPoints, int scanNumber, int msLevel,
	    double retentionTime, double precursorMZ, int precursorCharge,
	    int fragmentScans[], MassSpectrumType spectrumType,
	    PolarityType polarity, String scanDefinition,
	    Range<Double> scanMZRange, boolean centroided) {

	this.rawDataFile = rawDataFile;
	this.numberOfDataPoints = numberOfDataPoints;
	this.storageID = storageID;

	this.scanNumber = scanNumber;
	this.msLevel = msLevel;
	this.retentionTime = retentionTime;
	this.precursorMZ = precursorMZ;
	this.precursorCharge = precursorCharge;
	this.fragmentScans = fragmentScans;
	this.spectrumType = spectrumType;
	this.polarity = polarity;
	this.scanDefinition = scanDefinition;
	this.scanMZRange = scanMZRange;
	this.centroided = centroided;
    }

    /**
     * @return Scan's datapoints from temporary file.
     */
    public @Nonnull DataPoint[] getDataPoints() {

	try {
	    DataPoint result[] = rawDataFile.readDataPoints(storageID);
	    return result;
	} catch (IOException e) {
	    logger.severe("Could not read data from temporary file "
		    + e.toString());
	    return new DataPoint[0];
	}

    }

    /**
     * @return Returns scan datapoints within a given range
     */
    public @Nonnull DataPoint[] getDataPointsByMass(
	    @Nonnull Range<Double> mzRange) {

	DataPoint dataPoints[] = getDataPoints();

	int startIndex, endIndex;
	for (startIndex = 0; startIndex < dataPoints.length; startIndex++) {
	    if (dataPoints[startIndex].getMZ() >= mzRange.lowerEndpoint()) {
		break;
	    }
	}

	for (endIndex = startIndex; endIndex < dataPoints.length; endIndex++) {
	    if (dataPoints[endIndex].getMZ() > mzRange.upperEndpoint()) {
		break;
	    }
	}

	DataPoint pointsWithinRange[] = new DataPoint[endIndex - startIndex];

	// Copy the relevant points
	System.arraycopy(dataPoints, startIndex, pointsWithinRange, 0, endIndex
		- startIndex);

	return pointsWithinRange;
    }

    /**
     * @return Returns scan datapoints over certain intensity
     */
    public @Nonnull DataPoint[] getDataPointsOverIntensity(double intensity) {
	int index;
	Vector<DataPoint> points = new Vector<DataPoint>();
	DataPoint dataPoints[] = getDataPoints();

	for (index = 0; index < dataPoints.length; index++) {
	    if (dataPoints[index].getIntensity() >= intensity) {
		points.add(dataPoints[index]);
	    }
	}

	DataPoint pointsOverIntensity[] = points.toArray(new DataPoint[0]);

	return pointsOverIntensity;
    }

    public @Nonnull RawDataFile getDataFile() {
	return rawDataFile;
    }

    public int getStorageID() {
	return storageID;
    }

    /**
     * @see net.sf.mzmine.datamodel.Scan#getNumberOfDataPoints()
     */
    public int getNumberOfDataPoints() {
	return numberOfDataPoints;
    }

    /**
     * @see net.sf.mzmine.datamodel.Scan#getScanNumber()
     */
    public int getScanNumber() {
	return scanNumber;
    }

    /**
     * @see net.sf.mzmine.datamodel.Scan#getMSLevel()
     */
    public int getMSLevel() {
	return msLevel;
    }

    /**
     * @see net.sf.mzmine.datamodel.Scan#getPrecursorMZ()
     */
    public double getPrecursorMZ() {
	return precursorMZ;
    }

    /**
     * @return Returns the precursorCharge.
     */
    public int getPrecursorCharge() {
	return precursorCharge;
    }

    /**
     * @see net.sf.mzmine.datamodel.Scan#getScanAcquisitionTime()
     */
    public double getRetentionTime() {
	return retentionTime;
    }

    void updateValues() {
	DataPoint dataPoints[] = getDataPoints();

	// find m/z range and base peak
	if (dataPoints.length > 0) {

	    basePeak = dataPoints[0];
	    mzRange = Range.singleton(dataPoints[0].getMZ());
	    double tic = 0;

	    for (DataPoint dp : dataPoints) {

		if (dp.getIntensity() > basePeak.getIntensity())
		    basePeak = dp;

		mzRange = mzRange.span(Range.singleton(dp.getMZ()));

		tic += dp.getIntensity();

	    }

	    totalIonCurrent = new Double(tic);

	} else {
	    mzRange = Range.singleton(0.0);
	    totalIonCurrent = new Double(0);
	}
    }

    /**
     * @see net.sf.mzmine.datamodel.Scan#getMZRangeMax()
     */
    public @Nonnull Range<Double> getDataPointMZRange() {
	if (mzRange == null)
	    updateValues();
	return mzRange;
    }

    /**
	 * @see net.sf.mzmine.data.Scan#getBasePeakMZ()
	 */
	public DataPoint getBasePeak() {
		if ((basePeak == null) && (numberOfDataPoints > 0))
			updateValues();
		return basePeak;
	}

	/**
     * @see net.sf.mzmine.datamodel.Scan#getBasePeakMZ()
     */
    public DataPoint getHighestDataPoint() {
	if ((basePeak == null) && (numberOfDataPoints > 0))
	    updateValues();
	return basePeak;
    }

    /**
     * @see net.sf.mzmine.datamodel.Scan#getFragmentScanNumbers()
     */
    public int[] getFragmentScanNumbers() {
	return fragmentScans;
    }

    /**
     * @param fragmentScans
     *            The fragmentScans to set.
     */
    void setFragmentScanNumbers(int[] fragmentScans) {
	this.fragmentScans = fragmentScans;
    }

    /**
     * @see net.sf.mzmine.datamodel.Scan#getSpectrumType()
     */
    public MassSpectrumType getSpectrumType() {
	if (spectrumType == null) {
	    spectrumType = ScanUtils.detectSpectrumType(getDataPoints());
	}
	return spectrumType;
    }

    /**
	 * @see net.sf.mzmine.data.Scan#isCentroided()
	 */
	public boolean isCentroided() {
		return centroided;
	}
	
	public double getTIC() {
	if (totalIonCurrent == null)
	    updateValues();
	return totalIonCurrent;
    }

    @Override
    public String toString() {
	return ScanUtils.scanToString(this);
    }

    @Override
    public synchronized void addMassList(final @Nonnull MassList massList) {

	// Remove all mass lists with same name, if there are any
	MassList currentMassLists[] = massLists.toArray(new MassList[0]);
	for (MassList ml : currentMassLists) {
	    if (ml.getName().equals(massList.getName()))
		removeMassList(ml);
	}

	StorableMassList storedMassList;
	if (massList instanceof StorableMassList) {
	    storedMassList = (StorableMassList) massList;
	} else {
	    DataPoint massListDataPoints[] = massList.getDataPoints();
	    try {
		int mlStorageID = rawDataFile
			.storeDataPoints(massListDataPoints);
		storedMassList = new StorableMassList(rawDataFile, mlStorageID,
			massList.getName(), this);
	    } catch (IOException e) {
		logger.severe("Could not write data to temporary file "
			+ e.toString());
		return;
	    }
	}

	// Add the new mass list
	massLists.add(storedMassList);

	// Add the mass list to the tree model
	MZmineProjectImpl project = (MZmineProjectImpl) MZmineCore
		.getProjectManager().getCurrentProject();

	// Check if we are adding to the current project
	if (Arrays.asList(project.getDataFiles()).contains(rawDataFile)) {
	    final RawDataTreeModel treeModel = project.getRawDataTreeModel();
	    final MassList newMassList = storedMassList;
	    Runnable swingCode = new Runnable() {
		@Override
		public void run() {
		    treeModel.addObject(newMassList);
		}
	    };

	    try {
		if (SwingUtilities.isEventDispatchThread())
		    swingCode.run();
		else
		    SwingUtilities.invokeAndWait(swingCode);
	    } catch (Exception e) {
		e.printStackTrace();
	    }

	}

    }

    @Override
    public synchronized void removeMassList(final @Nonnull MassList massList) {

	// Remove the mass list
	massLists.remove(massList);
	if (massList instanceof StorableMassList) {
	    StorableMassList storableMassList = (StorableMassList) massList;
	    storableMassList.removeStoredData();
	}

	// Remove from the tree model
	MZmineProjectImpl project = (MZmineProjectImpl) MZmineCore
		.getProjectManager().getCurrentProject();

	// Check if we are using the current project
	if (Arrays.asList(project.getDataFiles()).contains(rawDataFile)) {
	    final RawDataTreeModel treeModel = project.getRawDataTreeModel();
	    Runnable swingCode = new Runnable() {
		@Override
		public void run() {
		    treeModel.removeObject(massList);
		}
	    };

	    SwingUtilities.invokeLater(swingCode);

	}

    }

    @Override
    public @Nonnull MassList[] getMassLists() {
	return massLists.toArray(new MassList[0]);
    }

    @Override
    public MassList getMassList(@Nonnull String name) {
	for (MassList ml : massLists) {
	    if (ml.getName().equals(name))
		return ml;
	}
	return null;
    }

    @Override
    public @Nonnull PolarityType getPolarity() {
	if (polarity == null)
	    polarity = PolarityType.UNKNOWN;
	return polarity;
    }

    @Override
    public String getScanDefinition() {
	if (scanDefinition == null)
	    scanDefinition = "";
	return scanDefinition;
    }

    @Override
    public @Nonnull Range<Double> getScanningMZRange() {
	if (scanMZRange == null)
	    scanMZRange = getDataPointMZRange();
	return scanMZRange;
    }
    
    /**
     * Get the filename that the scan or mass list would be exported to by default
     * 
     * @param massListName	// if empty, return scan export filename
     * @return
     */
    public String exportFilename(String massListName)
    {
    	String filename = FilenameUtils.removeExtension(getDataFile().getName())
    			+ ".ms" + getMSLevel() + "scan" + String.format("%04d", getScanNumber())
    			+ (massListName.isEmpty() ? "" : ".peaks_" + massListName)
    			+ ".txt";
    	return filename;
    }
    
    /**
     * Open the proper type of buffered file depending on the .gz suffix
     * 
     * @param path
     * @param filedata
     * @return
     * @throws IOException
     */
    private BufferedWriter openFile(String path) throws IOException
    {
    	BufferedWriter fd;
    	if (path.endsWith(".gz"))
    		fd = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(path))));
    	else
    		fd = new BufferedWriter(new FileWriter(path));
    	return fd;
    	}
    
    /**
     * Export the scan or mass list to a text file in two column tab-delimited format.
     * Return the number of datapoints in the scan or mass list.
     * 
     * @param massListName		// if empty, export scan data points
     * @param saveDirectory		// if empty, saveDirectory isn't pre-pended to filename before export
     * @param filename			// if empty, filename will be generated from scan information
     * @return					// number of points, 0 if requested mass list not found not found
     */
    public int exportToFile(String massListName, String saveDirectory, String filename)
    {
    	int exported = 0;

    	if (filename.isEmpty())
    		filename = exportFilename(massListName);

	if (!saveDirectory.isEmpty())
	    	filename = saveDirectory + File.separator + filename;

    	if (massListName.isEmpty())	// export scan
    	{
    		logger.info("Exporting scan " + getScanNumber() + " to file " + filename);
    		try
    		{
    			BufferedWriter fd = openFile(filename);
    			DataPoint pts[] = getDataPoints();
    			int num = pts.length;
    			fd.write("# Scan Number: "      + getScanNumber()   + "\n");
    			fd.write("# Scan MS Level: "    + getMSLevel()      + "\n");
    			fd.write("# Scan Data Points: " + num               + "\n");
    			fd.write("# Scan Mass Range: "  + (mzRange.upperEndpoint() - mzRange.lowerEndpoint()) + "\n");
    			fd.write("# Scan Min Mass: "    + mzRange.lowerEndpoint()  + "\n");
    			fd.write("# Scan Max Mass: "    + mzRange.upperEndpoint()  + "\n\n");
    			for (int p = 0; p < num; p++)
    				fd.write(pts[p].getMZ() + "\t" + pts[p].getIntensity() + "\n");
    			fd.close();
    			FileChecksum chksum = new FileChecksum(filename);
    			chksum.hash_file();
    			chksum.append_txt(false);
    			exported = num;
    		}
    		catch (Exception ex)
    		{
    			Logger.getLogger(ListExportTask.class.getName()).log(Level.SEVERE, "Failed writing scan file, " + filename, ex);
    		}
    	}
    	else						// export given mass list
    	{
    		MassList massList = getMassList(massListName);
    		if (massList != null)	// Skip those scans which do not have a mass list of given name
    		{
    			logger.info("Exporting mass list " + massListName + " for scan "+ getScanNumber() + " to file " + filename);
    			try
    			{
    				BufferedWriter fd = openFile(filename);
    				DataPoint mzPeaks[] = massList.getDataPoints();
    				int num = mzPeaks.length;
    				fd.write("# MS Level: "    + getMSLevel()    + "\n");
    				fd.write("# Scan: "        + getScanNumber() + "\n");
    				fd.write("# Mass List: "   + massListName    + "\n");
    				fd.write("# Data Points: " + num             + "\n");
    				for (int p = 0; p < num; p++)
    				{
    					DataPoint pt = mzPeaks[p];
    					fd.write(pt.getMZ() + "\t" + pt.getIntensity() + "\n");
    				}
    				fd.close();
    				FileChecksum chksum = new FileChecksum(filename);
    				chksum.hash_file();
    				chksum.append_txt(false);
    				exported = num;
    			}
    			catch (Exception ex)
    			{
    				Logger.getLogger(ListExportTask.class.getName()).log(Level.SEVERE, "Failed writing mass list file, " + filename, ex);
    			}
    		}
    	}
    	return exported;
    }
}
