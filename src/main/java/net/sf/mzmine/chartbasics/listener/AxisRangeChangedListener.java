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

package net.sf.mzmine.chartbasics.listener;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.data.Range;
import net.sf.mzmine.chartbasics.gui.wrapper.ChartViewWrapper;

public abstract class AxisRangeChangedListener implements AxisChangeListener {

  // last lower / upper range
  private Range lastRange = null;
  private ChartViewWrapper chart;

  public AxisRangeChangedListener(ChartViewWrapper cp) {
    chart = cp;
  }

  @Override
  public void axisChanged(AxisChangeEvent e) {
    ValueAxis a = (ValueAxis) e.getAxis();
    Range r = a.getRange();

    if (r != null && (lastRange == null || !r.equals(lastRange))) {
      // range has changed
      axisRangeChanged(chart, a, lastRange, r);
    }
    lastRange = r;
  }

  /**
   * only if axis range has changed
   * 
   * @param axis
   * @param lastR
   * @param newR
   */
  public abstract void axisRangeChanged(ChartViewWrapper chart, ValueAxis axis, Range lastR,
      Range newR);
}
