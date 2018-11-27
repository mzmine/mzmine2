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
package net.sf.mzmine.parameters.parametertypes;

import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.sf.mzmine.util.maths.CenterFunction;
import net.sf.mzmine.util.maths.CenterMeasure;
import net.sf.mzmine.util.maths.Transform;

/**
 * Parameter for center measure: median, avg, weighted avg
 * 
 */
public class CenterMeasureComponent extends JPanel {

  private static final long serialVersionUID = 1L;

  private final JComboBox<CenterMeasure> comboCenterMeasure;
  private final JComboBox<Transform> comboTransform;
  private JLabel labelTrans;

  public CenterMeasureComponent() {
    this(CenterMeasure.values(), Transform.values());
  }

  public CenterMeasureComponent(CenterMeasure choices[]) {
    this(choices, Transform.values());
  }

  public CenterMeasureComponent(Transform[] avgTransform) {
    this(CenterMeasure.values(), avgTransform);
  }

  public CenterMeasureComponent(CenterMeasure choices[], Transform[] avgTransform) {
    this(choices, avgTransform, CenterMeasure.values()[0], Transform.values()[0]);
  }

  /**
   * 
   * @param choices
   * @param avgTransform
   * @param selected selected center measure
   * @param selWeighting selected weighting
   */
  public CenterMeasureComponent(CenterMeasure choices[], Transform[] avgTransform,
      CenterMeasure selected, Transform selWeighting) {
    setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
    comboCenterMeasure = new JComboBox<>(choices);
    add(comboCenterMeasure);
    labelTrans = new JLabel("weighting: ");
    add(labelTrans);
    comboTransform = new JComboBox<>(avgTransform);
    comboTransform.setSelectedItem(selWeighting);
    add(comboTransform);

    // do not show weighting for median
    comboCenterMeasure.addItemListener(il -> {
      boolean visible = comboCenterMeasure.getSelectedItem().equals(CenterMeasure.AVG);
      comboTransform.setVisible(visible);
      labelTrans.setVisible(visible);
      revalidate();
      repaint();
    });
    comboCenterMeasure.setSelectedItem(selected);
  }

  @Override
  public void setToolTipText(String toolTip) {
    comboCenterMeasure.setToolTipText(toolTip);
  }

  public CenterFunction getSelectedFunction() {
    CenterMeasure measure = (CenterMeasure) comboCenterMeasure.getSelectedItem();
    Transform trans = Transform.NONE;
    if (comboTransform.isVisible())
      trans = (Transform) comboTransform.getSelectedItem();
    return new CenterFunction(measure, trans);
  }

  public void setSelectedItem(CenterMeasure newValue, Transform transform) {
    comboCenterMeasure.setSelectedItem(newValue);
    comboTransform.setSelectedItem(transform);
  }

  @Override
  public void setEnabled(boolean enabled) {
    comboCenterMeasure.setEnabled(enabled);
    comboTransform.setEnabled(enabled);
  }

  public void addItemListener(ItemListener il) {
    comboCenterMeasure.addItemListener(il);
    comboTransform.addItemListener(il);
  }

  public void setSelectedItem(CenterFunction newValue) {
    comboTransform.setSelectedItem(newValue.getWeightTransform());
    comboCenterMeasure.setSelectedItem(newValue.getMeasure());
  }
}