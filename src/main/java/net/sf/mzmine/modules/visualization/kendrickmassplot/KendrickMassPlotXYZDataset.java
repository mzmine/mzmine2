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

package net.sf.mzmine.modules.visualization.kendrickmassplot;

import org.jfree.data.xy.AbstractXYZDataset;

import net.sf.mzmine.datamodel.PeakList;
import net.sf.mzmine.datamodel.PeakListRow;
import net.sf.mzmine.parameters.ParameterSet;

class KendrickMassPlotXYZDataset extends AbstractXYZDataset{

    /**
     * XYZDataset for Kendrick mass plots
     */
    private static final long serialVersionUID = 1L;

    private PeakListRow selectedRows[];
    private String yAxisKMBase;
    private String xAxisKMBase;
    private String zAxisKMBase;
    private double xAxisKMFactor = -1;
    private double yAxisKMFactor = -1;
    private double zAxisKMFactor = -1;
    private double[] xValues;
    private double[] yValues;
    private double[] zValues;

    public KendrickMassPlotXYZDataset(ParameterSet parameters) {

        PeakList peakList = parameters
                .getParameter(KendrickMassPlotParameters.peakList).getValue()
                .getMatchingPeakLists()[0];

        this.selectedRows = parameters
                .getParameter(KendrickMassPlotParameters.selectedRows).getMatchingRows(peakList);

        this.xAxisKMBase = parameters
                .getParameter(KendrickMassPlotParameters.xAxisValues).getValue();

        this.yAxisKMBase = parameters
                .getParameter(KendrickMassPlotParameters.yAxisValues).getValue();

        this.zAxisKMBase = parameters
                .getParameter(KendrickMassPlotParameters.zAxisValues).getValue();

        //Calc xValues
        xValues = new double[selectedRows.length];
        for (int i = 0; i < selectedRows.length; i++) {
            //simply plot m/z values as x axis
            if(xAxisKMBase.equals("m/z")) {
                xValues[i] = selectedRows[i].getAverageMZ();
            }
            //plot Kendrick masses as x axis
            else if(xAxisKMBase.equals("KM")) {
                xValues[i] = selectedRows[i].getAverageMZ()*
                        getxAxisKMFactor(yAxisKMBase);
            }
            //plot Kendrick mass defect (KMD) as x Axis to the base of H
            else if(xAxisKMBase.equals("KMD (H)")) {
                xValues[i] = (((int)selectedRows[i].getAverageMZ()*
                        getxAxisKMFactor(xAxisKMBase)+1)-selectedRows[i].getAverageMZ()*
                        getxAxisKMFactor(xAxisKMBase));
            }
            //plot Kendrick mass defect (KMD) as x Axis to the base of CH2
            else if(xAxisKMBase.equals("KMD (CH2)")) {
                xValues[i] = (((int)selectedRows[i].getAverageMZ()*
                        getxAxisKMFactor(xAxisKMBase)+1)-selectedRows[i].getAverageMZ()*
                        getxAxisKMFactor(xAxisKMBase));
            }
            //plot Kendrick mass defect (KMD) as x Axis to the base of O
            else if(xAxisKMBase.equals("KMD (O)")) {
                xValues[i] = (((int)selectedRows[i].getAverageMZ()*
                        getxAxisKMFactor(xAxisKMBase)+1)-selectedRows[i].getAverageMZ()*
                        getxAxisKMFactor(xAxisKMBase));
            }
        }

        //Calc yValues
        yValues = new double[selectedRows.length];
        for (int i = 0; i < selectedRows.length; i++) {
            //plot Kendrick mass defect (KMD) as y Axis to the base of H
            if(yAxisKMBase.equals("KMD (H)")) {
                yValues[i] = ((int)(selectedRows[i].getAverageMZ()*
                        getyAxisKMFactor(yAxisKMBase))+1)-selectedRows[i].getAverageMZ()*
                        getyAxisKMFactor(yAxisKMBase);
            }
            //plot Kendrick mass defect (KMD) as y Axis to the base of CH2
            else if(yAxisKMBase.equals("KMD (CH2)")) {
                yValues[i] = ((int)(selectedRows[i].getAverageMZ()*
                        getyAxisKMFactor(yAxisKMBase))+1)-selectedRows[i].getAverageMZ()*
                        getyAxisKMFactor(yAxisKMBase);
            }
            //plot Kendrick mass defect (KMD) as y Axis to the base of O
            else if(yAxisKMBase.equals("KMD (O)")) {
                yValues[i] = ((int)(selectedRows[i].getAverageMZ()*
                        getyAxisKMFactor(yAxisKMBase))+1)-selectedRows[i].getAverageMZ()*
                        getyAxisKMFactor(yAxisKMBase);
            }
        }

        //Calc zValues
        zValues = new double[selectedRows.length];
        for (int i = 0; i < selectedRows.length; i++) {
            //plot selected feature characteristic as z Axis
            if(zAxisKMBase.equals("Retention time")) {
                zValues[i] = selectedRows[i].getAverageRT();
            }
            else if(zAxisKMBase.equals("Intensity")) {
                zValues[i] = selectedRows[i].getAverageHeight();
            }
            else if(zAxisKMBase.equals("Area")) {
                zValues[i] = selectedRows[i].getAverageArea();
            }
            else if(zAxisKMBase.equals("Tailing factor")) {
                zValues[i] = selectedRows[i].getBestPeak().getTailingFactor();
            }
            else if(zAxisKMBase.equals("Asymmetry factor")) {
                zValues[i] = selectedRows[i].getBestPeak().getAsymmetryFactor();
            }
            else if(zAxisKMBase.equals("FWHM")) {
                zValues[i] = selectedRows[i].getBestPeak().getFWHM();
            }

            //plot Kendrick mass defect (KMD) as z Axis to the base of H
            if(zAxisKMBase.equals("KMD (H)")) {
                zValues[i] = ((int)(selectedRows[i].getAverageMZ()*
                        getzAxisKMFactor(zAxisKMBase))+1)-selectedRows[i].getAverageMZ()*
                        getzAxisKMFactor(zAxisKMBase);
            }
            //plot Kendrick mass defect (KMD) as z Axis to the base of CH2
            else if(zAxisKMBase.equals("KMD (CH2)")) {
                zValues[i] = ((int)(selectedRows[i].getAverageMZ()*
                        getzAxisKMFactor(zAxisKMBase))+1)-selectedRows[i].getAverageMZ()*
                        getzAxisKMFactor(zAxisKMBase);
            }
            //plot Kendrick mass defect (KMD) as z Axis to the base of O
            else if(zAxisKMBase.equals("KMD (O)")) {
                zValues[i] = ((int)(selectedRows[i].getAverageMZ()*
                        getzAxisKMFactor(zAxisKMBase))+1)-selectedRows[i].getAverageMZ()*
                        getzAxisKMFactor(zAxisKMBase);
            }

            if(zAxisKMBase.equals("m/z")) {
                zValues[i] = selectedRows[i].getAverageMZ();
            }
        }
    }
    //Calculate xAxis Kendrick mass factor (KM factor)
    private double getxAxisKMFactor(String xAxisKMBase) {
        if(xAxisKMFactor==-1) {
            if(xAxisKMBase.equals("KMD (CH2)")) {
                xAxisKMFactor = (14/14.01565006);
            }
            else if(xAxisKMBase.equals("KMD (H)")) {
                xAxisKMFactor = (1/1.007825037);
            }
            else if(xAxisKMBase.equals("KMD (O)")) {
                xAxisKMFactor = (16/15.994915);
            }
            else {
                xAxisKMFactor = 0;
            }
        }
        return xAxisKMFactor;
    }

    //Calculate yAxis Kendrick mass factor (KM factor)
    private double getyAxisKMFactor(String yAxisKMBase) {
        if(yAxisKMFactor==-1) {
            if(yAxisKMBase.equals("KMD (CH2)")) {
                yAxisKMFactor = (14/14.01565006);
            }
            else if(yAxisKMBase.equals("KMD (H)")) {
                yAxisKMFactor = (1/1.007825037);
            }
            else if(yAxisKMBase.equals("KMD (O)")) {
                yAxisKMFactor = (16/15.994915);
            }
            else {
                yAxisKMFactor = 0;
            }
        }
        return yAxisKMFactor;
    }

    //Calculate yAxis Kendrick mass factor (KM factor)
    private double getzAxisKMFactor(String zAxisKMBase) {
        if(zAxisKMFactor==-1) {
            if(zAxisKMBase.equals("KMD (CH2)")) {
                zAxisKMFactor = (14/14.01565006);
            }
            else if(zAxisKMBase.equals("KMD (H)")) {
                zAxisKMFactor = (1/1.007825037);
            }
            else if(zAxisKMBase.equals("KMD (O)")) {
                zAxisKMFactor = (16/15.994915);
            }
            else {      
                zAxisKMFactor = 0;
            }
        }
        return zAxisKMFactor;
    }


    @Override
    public int getItemCount(int series) {
        return selectedRows.length;
    }

    @Override
    public Number getX(int series, int item) {
        return xValues[item];
    }

    @Override
    public Number getY(int series, int item) {
        return yValues[item];
    }

    @Override
    public Number getZ(int series, int item) {
        return zValues[item];
    }

    @Override
    public int getSeriesCount() {
        return selectedRows.length;
    }

    public Comparable<?> getRowKey(int row) {
        return selectedRows[row].toString();
    }

    @Override
    public Comparable getSeriesKey(int series) {
        return getRowKey(series);
    }

}
