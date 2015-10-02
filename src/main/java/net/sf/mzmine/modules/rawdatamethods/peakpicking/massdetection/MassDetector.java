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

package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection;

import net.sf.mzmine.datamodel.DataPoint;
import net.sf.mzmine.datamodel.Scan;
import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.modules.MZmineModule;
import net.sf.mzmine.parameters.ParameterSet;

/**
 * 
 */
public interface MassDetector extends MZmineModule {
 
    /**
     * The detector can change the target name
     * 
     * @param targetName
     * @return
      */
    public String filterTargetName(String targetName);
    
    /**
     * Return the description of this task
     * 
     * @param
     * @param
     * @return
     */
    public String getDescription(String job, String str);
    
    /**
     * Returns the job id for getting mass values
     * @param dataFile 
     * @param targetName
     * @param parameterSet
     * @param scanCount
     * @return
     */
    public String startMassValuesJob(RawDataFile dataFile, String targetName, ParameterSet parameterSet, int scanCount);
     	
    /**
     * Returns mass and intensity values detected in given scan
     * 
     * @param san
     * @param selected
     * @param job
     * @param parameters
     * @return
     */	
    public DataPoint[] getMassValues(Scan scan, boolean selected, String job, ParameterSet parameters);
    
    /**
     * Mark the job done
     * @param job 
     */
    public void finishMassValuesJob(String job);
}
