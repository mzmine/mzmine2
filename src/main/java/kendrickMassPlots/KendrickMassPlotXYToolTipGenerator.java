/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2016, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * -------------------------------
 * StandardXYToolTipGenerator.java
 * -------------------------------
 * (C) Copyright 2004-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 12-May-2004 : Version 1 (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 25-Jan-2007 : Added new constructor - see bug 1624067 (DG);
 *
 */

package kendrickMassPlots;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;

public class KendrickMassPlotXYToolTipGenerator implements XYToolTipGenerator {

	private String xAxisLabel, yAxisLabel;
	private NumberFormat numberFormatX = new DecimalFormat("####0.0000");
	private NumberFormat numberFormatY = new DecimalFormat("0.000");

	public KendrickMassPlotXYToolTipGenerator(String xAxisLabel, String yAxisLabel){
		super();
		this.xAxisLabel = xAxisLabel;
		this.yAxisLabel = yAxisLabel;

	}
	public String generateToolTip(XYDataset dataset, int series, int item) {
		return String.valueOf(xAxisLabel+": "+
				numberFormatX.format(dataset.getXValue(series, item))+
				" "+yAxisLabel+": "+
				numberFormatY.format(dataset.getYValue(series, item)));
	}

}
