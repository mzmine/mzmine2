/*
 * Copyright 2006-2018 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MZmine 2; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package net.sf.mzmine.modules.peaklistmethods.identification.sirius;

import static net.sf.mzmine.modules.peaklistmethods.identification.sirius.SingleRowIdentificationParameters.ELEMENTS;
import static net.sf.mzmine.modules.peaklistmethods.identification.sirius.SingleRowIdentificationParameters.FINGERID_CANDIDATES;
import static net.sf.mzmine.modules.peaklistmethods.identification.sirius.SingleRowIdentificationParameters.MZ_TOLERANCE;
import static net.sf.mzmine.modules.peaklistmethods.identification.sirius.SingleRowIdentificationParameters.NEUTRAL_MASS;
import static net.sf.mzmine.modules.peaklistmethods.identification.sirius.SingleRowIdentificationParameters.SIRIUS_CANDIDATES;

import de.unijena.bioinf.ChemistryBase.chem.FormulaConstraints;
import de.unijena.bioinf.ChemistryBase.ms.Ms2Experiment;
import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.IonAnnotation;
import io.github.msdk.datamodel.IonType;
import io.github.msdk.datamodel.MsSpectrum;
import io.github.msdk.id.sirius.ConstraintsGenerator;
import io.github.msdk.id.sirius.FingerIdWebMethod;
import io.github.msdk.id.sirius.SiriusIdentificationMethod;
import io.github.msdk.id.sirius.SiriusIonAnnotation;
import io.github.msdk.util.IonTypeUtil;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import net.sf.mzmine.datamodel.Feature;
import net.sf.mzmine.datamodel.IonizationType;
import net.sf.mzmine.datamodel.PeakListRow;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.parameters.parametertypes.tolerances.MZTolerance;
import net.sf.mzmine.taskcontrol.AbstractTask;
import net.sf.mzmine.taskcontrol.TaskPriority;
import net.sf.mzmine.taskcontrol.TaskStatus;
import org.openscience.cdk.formula.MolecularFormulaRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleRowIdentificationTask extends AbstractTask {
  public static final NumberFormat massFormater = MZmineCore.getConfiguration().getMZFormat();

  private double searchedMass;
  private MZTolerance mzTolerance;
  private PeakListRow peakListRow;
  private IonizationType ionType;
  private MolecularFormulaRange formulaRange;
  private Double parentMass;
  private Integer fingerCandidates;
  private Integer siriusCandidates;
  private List<FingerIdWebMethodTask> fingerTasks;

  private static final Logger logger = LoggerFactory.getLogger(SingleRowIdentificationTask.class);

  /**
   * Create the task.
   * 
   * @param parameters task parameters.
   * @param peakListRow peak-list row to identify.
   */
  public SingleRowIdentificationTask(ParameterSet parameters, PeakListRow peakListRow) {

    this.peakListRow = peakListRow;

    searchedMass = parameters.getParameter(NEUTRAL_MASS).getValue();
    mzTolerance = parameters.getParameter(MZ_TOLERANCE).getValue();
    siriusCandidates = parameters.getParameter(SIRIUS_CANDIDATES).getValue();
    fingerCandidates = parameters.getParameter(FINGERID_CANDIDATES).getValue();

    ionType = parameters.getParameter(NEUTRAL_MASS).getIonType();
    parentMass = parameters.getParameter(NEUTRAL_MASS).getValue();

    formulaRange = parameters.getParameter(ELEMENTS).getValue();
  }

  /**
   * @see net.sf.mzmine.taskcontrol.Task#getFinishedPercentage()
   */
  public double getFinishedPercentage() {
    //TODO: refactor
    if (isFinished())
      return 100.0;
    else if (fingerTasks != null) {
      int amount = fingerTasks.size();
      double value = 0;
      for (FingerIdWebMethodTask t: fingerTasks)
        value += t.getFinishedPercentage();
      value /= amount;

      return value;
    }
    return 0;
  }

  //TODO: refactor
  public String getTaskDescription() {
    return "Peak identification of " + massFormater.format(searchedMass) + " using Sirius module";
  }

  /**
   * @see Runnable#run()
   */
  public void run() {
    setStatus(TaskStatus.PROCESSING);

    NumberFormat massFormater = MZmineCore.getConfiguration().getMZFormat();

    ResultWindow window = new ResultWindow(peakListRow, searchedMass, this);
    window.setTitle("Sirius makes fun " + massFormater.format(searchedMass) + " amu");
    window.setVisible(true);

    Feature bestPeak = peakListRow.getBestPeak();
    SpectrumProcessing processor = new SpectrumProcessing(bestPeak);
    List<MsSpectrum> ms1list = processor.getMsList();
    List<MsSpectrum> ms2list = processor.getMsMsList();

    /* Debug */
    processor.saveSpectrum(processor.getPeakName() + "_ms1.txt", 1);
    processor.saveSpectrum(processor.getPeakName() + "_ms2.txt", 2);

    SiriusIdentificationMethod siriusMethod = null;
    try {
      siriusMethod = MethodsExecution.generateSiriusMethod(ms1list, ms2list, formulaRange, mzTolerance.getPpmTolerance(), ionType, parentMass, siriusCandidates);
      siriusMethod.execute();
    } catch (MSDKException e) {
      logger.error("Internal error of Sirius MSDK module appeared");
      e.printStackTrace();
    }
    /* TODO: If code below will failure, then siriusMethod will be null... Unhandled Null-pointer exception? */
    // TODO: use a HEAP to sort items
    //TODO SORT ITEMS BY FINGERID SCORE

    if (processor.peakContainsMsMs()) {
//      try {
        Ms2Experiment experiment = siriusMethod.getExperiment();
        fingerTasks = MethodsExecution.generateFingerIdWebMethods(siriusMethod.getResult(), experiment, fingerCandidates, window);
////        fingerTasks = new LinkedList<>();
//
//      /* // Serial processing
//      for (IonAnnotation ia: siriusMethod.getResult()) {
//        SiriusIonAnnotation annotation = (SiriusIonAnnotation) ia;
//        List<IonAnnotation> fingerResults = processFingerId(annotation, siriusMethod.getExperiment());
//        items.addAll(fingerResults);
//      } */
//        for (IonAnnotation ia : siriusMethod.getResult()) {
//          SiriusIonAnnotation annotation = (SiriusIonAnnotation) ia;
//          FingerIdWebMethodTask task = new FingerIdWebMethodTask(annotation, experiment, fingerCandidates, window);
//          fingerTasks.add(task);
//          MZmineCore.getTaskController().addTask(task, TaskPriority.NORMAL);
//        }
//        Thread.sleep(1000);
//      } catch (InterruptedException interrupt) {
//        logger.error("Processing of FingerWebMethods were interrupted");
//        interrupt.printStackTrace();
//      }
    } else {
      window.addListofItems(siriusMethod.getResult());
    }

    setStatus(TaskStatus.FINISHED);
  }
}
