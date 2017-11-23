package kendrickMassPlots;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.AxisCollection;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.StandardXYSeriesLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.category.StatisticalLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYZDataset;

import figs.Chart;
import net.sf.mzmine.datamodel.PeakList;
import net.sf.mzmine.desktop.impl.WindowsMenu;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.parameters.parametertypes.DoubleParameter;
import net.sf.mzmine.parameters.parametertypes.WindowSettingsParameter;

public class KendrickMassPlotWindow  extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static final Font legendFont = new Font("SansSerif", Font.PLAIN, 10);
	static final Font titleFont = new Font("SansSerif", Font.PLAIN, 11);

	private Logger logger = Logger.getLogger(this.getClass().getName());

	private XYDataset dataset2D;
	private XYZDataset dataset3D;
	private JFreeChart chart;

	public KendrickMassPlotWindow(ParameterSet parameters) {

		PeakList peakList = parameters
				.getParameter(KendrickMassPlotParameters.peakList).getValue()
				.getMatchingPeakLists()[0];

		String title = "Kendrick mass plot [" + peakList + "]";
		String xAxisLabel = parameters
				.getParameter(KendrickMassPlotParameters.xAxisValues)
				.getValue();
		String yAxisLabel = parameters
				.getParameter(KendrickMassPlotParameters.yAxisValues)
				.getValue();
		String zAxisLabel = parameters
				.getParameter(KendrickMassPlotParameters.zAxisValues)
				.getValue();

		// create dataset
		//2D, if no third dimension was selected
		if(parameters.getParameter(KendrickMassPlotParameters.zAxisValues).getValue().equals("none")) {
			dataset2D = new KendrickMassPlotXYDataset(parameters);
			logger.finest("Creating new chart instance");

			chart = ChartFactory.createScatterPlot(title, xAxisLabel, yAxisLabel,
					dataset2D, PlotOrientation.VERTICAL, true, true, false);

			XYPlot plot = (XYPlot) chart.getPlot();
			plot.setBackgroundPaint(Color.WHITE);

			// set renderer
			XYBlockRenderer renderer = new XYBlockRenderer();
			for (int i = 0; i < plot.getAnnotations().size(); i++) {
				System.out.println(plot.getAnnotations().get(i));
			}
			renderer.setSeriesPaint(0, Color.GREEN);
			renderer.setBlockWidth(1);
			renderer.setBlockHeight(0.002);
			StandardXYToolTipGenerator tooltipGenerator = new StandardXYToolTipGenerator();
			renderer.setSeriesToolTipGenerator(0, tooltipGenerator);
			plot.setRenderer(renderer);
		}
		//3D, if a third dimension was selected
		else{
			dataset3D = new KendrickMassPlotXYZDataset(parameters);
			double[] copyZValues = new double[dataset3D.getItemCount(0)];
			for (int i = 0; i < dataset3D.getItemCount(0); i++) {
				copyZValues[i] = dataset3D.getZValue(0, i);
			}
			Arrays.sort(copyZValues);
			double min = copyZValues[0];
			double max = copyZValues[copyZValues.length-1];
			logger.finest("Creating new chart instance");

			chart = ChartFactory.createScatterPlot(title, xAxisLabel, yAxisLabel,
					dataset3D, PlotOrientation.VERTICAL, true, true, false);
			XYBlockRenderer renderer = new XYBlockRenderer();
			Paint[] contourColors = null;
			LookupPaintScale scale = null;

			contourColors = KendrickMassPlotPaintScales.getFullRainBowScale();
			scale = new LookupPaintScale(min, max, Color.white);

			double [] scaleValues = new double[contourColors.length];
			double delta = (max - min)/(contourColors.length -1);
			double value = min;
			for(int i=0; i<contourColors.length; i++){
				scale.add(value, contourColors[i]);
				scaleValues[i] = value;
				value = value + delta;
			}

			renderer.setPaintScale(scale);
			renderer.setBlockWidth(1);
			renderer.setBlockHeight(0.002);
			renderer.setSeriesToolTipGenerator(0, new StandardXYToolTipGenerator());
			XYPlot plot = chart.getXYPlot();
			plot.setRenderer(renderer);
			plot.setBackgroundPaint(Color.white);
			plot.setDomainGridlinesVisible(false);
			plot.setRangeGridlinePaint(Color.white);
			plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));
			plot.setOutlinePaint(Color.black);
			plot.setBackgroundPaint(Color.white);
			//Legend
			NumberAxis scaleAxis = new NumberAxis(zAxisLabel);
			scaleAxis.setRange(min, max);
			scaleAxis.setAxisLinePaint(Color.white);
			scaleAxis.setTickMarkPaint(Color.white);
			PaintScaleLegend legend = new PaintScaleLegend(scale, scaleAxis);//new PaintScaleLegend(new GrayPaintScale(), scaleAxis);
			legend.setStripOutlineVisible(false);
			legend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
			legend.setAxisOffset(5.0);
			legend.setMargin(new RectangleInsets(5, 5, 5, 5));
			legend.setFrame(new BlockBorder(Color.white));
			legend.setPadding(new RectangleInsets(10, 10, 10, 10));
			legend.setStripWidth(10);
			legend.setPosition(RectangleEdge.LEFT);
			chart.addSubtitle(legend);

		}

		chart.setBackgroundPaint(Color.white);

		// create chart JPanel
		ChartPanel chartPanel = new ChartPanel(chart);
		add(chartPanel, BorderLayout.CENTER);



		// disable maximum size (we don't want scaling)
		chartPanel.setMaximumDrawWidth(Integer.MAX_VALUE);
		chartPanel.setMaximumDrawHeight(Integer.MAX_VALUE);

		// set title properties
		TextTitle chartTitle = chart.getTitle();
		chartTitle.setMargin(5, 0, 0, 0);
		chartTitle.setFont(titleFont);

		LegendTitle legend = chart.getLegend();
		legend.setItemFont(legendFont);
		legend.setBorder(0, 0, 0, 0);
		legend.setVisible(false);


		setTitle(title);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBackground(Color.white);

		// Add the Windows menu
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(new WindowsMenu());
		setJMenuBar(menuBar);

		pack();

		// get the window settings parameter
		ParameterSet paramSet = MZmineCore.getConfiguration()
				.getModuleParameters(KendrickMassPlotModule.class);
		WindowSettingsParameter settings = paramSet
				.getParameter(KendrickMassPlotParameters.windowSettings);

		// update the window and listen for changes
		settings.applySettingsToWindow(this);
		this.addComponentListener(settings);

	}

	JFreeChart getChart() {
		return chart;
	}

}
