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

package net.sf.mzmine.datamodel;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import net.sf.mzmine.datamodel.impl.RemoteJob;

import com.google.common.collect.Range;

public interface RawDataFile {

    /**
     * Returns the name of this data file (can be a descriptive name, not
     * necessarily the original file name)
     */
    @Nonnull
    public String getName();

    /**
     * Change the name of this data file
     */
    public void setName(@Nonnull String name);

    public int getNumOfScans();

    public int getNumOfScans(int msLevel);

    /**
     * Returns sorted array of all MS levels in this file
     */
    @Nonnull
    public int[] getMSLevels();

    /**
     * Returns sorted array of all scan numbers in this file
     * 
     * @return Sorted array of scan numbers, never returns null
     */
    @Nonnull
    public int[] getScanNumbers();

    /**
     * Returns sorted array of all scan numbers in given MS level
     * 
     * @param msLevel
     *            MS level
     * @return Sorted array of scan numbers, never returns null
     */
    @Nonnull
    public int[] getScanNumbers(int msLevel);

    /**
     * Returns sorted array of all scan numbers in given MS level and retention
     * time range
     * 
     * @param msLevel
     *            MS level
     * @param rtRange
     *            Retention time range
     * @return Sorted array of scan numbers, never returns null
     */
    @Nonnull
    public int[] getScanNumbers(int msLevel, @Nonnull Range<Double> rtRange);

    /**
     * 
     * @param scan
     *            Desired scan number
     * @return Desired scan
     */
    @Nonnull
    public Scan getScan(int scan);

    @Nonnull
    public Range<Double> getDataMZRange();

    @Nonnull
    public Range<Double> getDataRTRange();

    @Nonnull
    public Range<Double> getDataMZRange(int msLevel);

    @Nonnull
    public Range<Double> getDataRTRange(int msLevel);

    public double getDataMaxBasePeakIntensity(int msLevel);

    public double getDataMaxTotalIonCurrent(int msLevel);
    
    public void addJob(String name, RawDataFile raw, String targetName);
    public void removeJob(String name);
    public ArrayList<RemoteJob> getJobs();
    public RemoteJob getJob(String compoundName);

    /**
     * Close the file in case it is removed from the project
     */
    public void close();

}
