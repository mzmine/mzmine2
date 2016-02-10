package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator;

import static org.junit.Assert.*;

import java.awt.Window;

import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.datamodel.Scan;
import net.sf.mzmine.desktop.preferences.MZminePreferences;
import net.sf.mzmine.util.ExitCode;
import net.sf.mzmine.util.dialogs.HeadlessDialogFactory;

import org.junit.Test;
import org.mockito.ArgumentMatcher;

import static org.mockito.Mockito.*;

import com.google.common.collect.Range;
import com.veritomyx.PeakInvestigatorSaaS;
import com.veritomyx.VeritomyxSettings;
import com.veritomyx.actions.BaseAction.ResponseFormatException;
import com.veritomyx.actions.BaseAction;
import com.veritomyx.actions.PiVersionsAction;

public class PeakInvestigatorParametersTest {

	/**
	 * When parameters are first created, the mass range should (0,
	 * Integer.MAX_VALUE). This tests for that condition, which should try to
	 * load data files to use for the range, and thus, throw a
	 * NullPointerException.
	 */
	@Test(expected = NullPointerException.class)
	public void testDefault_MassRange() {
		PeakInvestigatorParameters parameters = new PeakInvestigatorParameters();
		parameters.getMassRange();
	}

	public final static String VERSIONS_RESPONSE_1 = "{\"Action\":\"PI_VERSIONS\",\"Current\":\"1.2\",\"LastUsed\":\"\",\"Count\":3,\"Versions\":[\"1.2\",\"1.1\",\"1.0.0\"]}";
	public final static String VERSIONS_RESPONSE_2 = "{\"Action\":\"PI_VERSIONS\",\"Current\":\"1.2\",\"LastUsed\":\"1.0.0\",\"Count\":3,\"Versions\":[\"1.2\",\"1.1\",\"1.0.0\"]}";
	public final static String VERSIONS_RESPONSE_3 = "{\"Action\":\"PI_VERSIONS\",\"Current\":\"1.2\",\"LastUsed\":\"1.2\",\"Count\":3,\"Versions\":[\"1.2\",\"1.1\",\"1.0.0\"]}";

	@Test
	public void testFormatPiVersions_LastUsedEmpty()
			throws ResponseFormatException {
		PiVersionsAction action = new PiVersionsAction("server", "username",
				"password");
		action.processResponse(VERSIONS_RESPONSE_1);
		String[] choices = PeakInvestigatorParameters.formatPiVersions(action);
		assertArrayEquals(new String[] { "1.2 (current)", "1.1", "1.0.0" },
				choices);
	}

	@Test
	public void testFormatPiVersions_LastUsedSameAsOldestVersion()
			throws ResponseFormatException {
		PiVersionsAction action = new PiVersionsAction("server", "username",
				"password");
		action.processResponse(VERSIONS_RESPONSE_2);
		String[] choices = PeakInvestigatorParameters.formatPiVersions(action);
		assertArrayEquals(new String[] { "1.2 (current)", "1.1",
				"1.0.0 (last used)" }, choices);
	}

	@Test
	public void testFormatPiVersions_LastUsedSameAsCurrentVersion()
			throws ResponseFormatException {
		PiVersionsAction action = new PiVersionsAction("server", "username",
				"password");
		action.processResponse(VERSIONS_RESPONSE_3);
		String[] choices = PeakInvestigatorParameters.formatPiVersions(action);
		assertArrayEquals(new String[] { "1.2 (current and last used)", "1.1",
				"1.0.0" }, choices);
	}

	@Test
	public void testPerformPiVersionCall_OkOnFirstTry()
			throws ResponseFormatException {

		// mock preferences, returning a dummy settings
		VeritomyxSettings settings = new VeritomyxSettings("test", "username",
				"password", 0);
		MZminePreferences preferences = mock(MZminePreferences.class);
		when(preferences.getVeritomyxSettings()).thenReturn(settings);

		// mock PI service to return as if everything is OK
		PeakInvestigatorSaaS service = mock(PeakInvestigatorSaaS.class);
		when(service.executeAction(argThat(new IsPiVersionsAction())))
				.thenReturn(VERSIONS_RESPONSE_1);

		PiVersionsAction action = PeakInvestigatorParameters
				.performPiVersionsCall(preferences, service, null);
		assertFalse(action.hasError());
	}

	@Test
	public void testPerformPiVersionCall_BadOnFirstTryThenCancel()
			throws ResponseFormatException {

		VeritomyxSettings settings = new VeritomyxSettings("test", "username",
				"password", 0);

		// mock preferences, and return dummy settings and Cancel exitcode from
		// dialog
		IsWindowOrNull matcher = new IsWindowOrNull();
		MZminePreferences preferences = mock(MZminePreferences.class);
		when(preferences.getVeritomyxSettings()).thenReturn(settings);
		when(preferences.showSetupDialog(argThat(matcher), anyBoolean()))
				.thenReturn(ExitCode.CANCEL);

		// mock PI service, and then return error due to bad credentials
		PeakInvestigatorSaaS service = mock(PeakInvestigatorSaaS.class);
		String response = BaseAction.ERROR_CREDENTIALS.replace("ACTION",
				"PI_VERSIONS");
		when(service.executeAction(argThat(new IsPiVersionsAction())))
				.thenReturn(response);

		PeakInvestigatorParameters.setDialogFactory(new HeadlessDialogFactory());
		PiVersionsAction action = PeakInvestigatorParameters
				.performPiVersionsCall(preferences, service, null);

		assertEquals(null, action);
	}

	@Test
	public void testPerformPiVersionCall_BadOnFirstTryThenGood()
			throws ResponseFormatException {

		VeritomyxSettings settings = new VeritomyxSettings("test", "username",
				"password", 0);

		// mock preferences, and return dummy settings and OK exitcode from
		// dialog
		IsWindowOrNull matcher = new IsWindowOrNull();
		MZminePreferences preferences = mock(MZminePreferences.class);
		when(preferences.getVeritomyxSettings()).thenReturn(settings);
		when(preferences.showSetupDialog(argThat(matcher), anyBoolean()))
				.thenReturn(ExitCode.OK);

		// mock PI service, and then first return error due to bad credentials
		// and then good response because of good credentials
		PeakInvestigatorSaaS service = mock(PeakInvestigatorSaaS.class);
		String response = BaseAction.ERROR_CREDENTIALS.replace("ACTION",
				"PI_VERSIONS");
		when(service.executeAction(argThat(new IsPiVersionsAction())))
				.thenReturn(response, VERSIONS_RESPONSE_1);

		PeakInvestigatorParameters.setDialogFactory(new HeadlessDialogFactory());
		PiVersionsAction action = PeakInvestigatorParameters
				.performPiVersionsCall(preferences, service, null);

		assertFalse(action.hasError());
	}

	public final static Range<Double> MASS_RANGE_1 = Range.closed(50.35, 100.7);
	public final static int[] TEST_CONTROL_1 = new int[] { 50, 101 };

	public final static Range<Double> MASS_RANGE_2A = Range.closed(60.35, 90.7);
	public final static Range<Double> MASS_RANGE_2B = Range.closed(40.35, 90.7);
	public final static Range<Double> MASS_RANGE_2C = Range
			.closed(60.35, 110.7);
	public final static Range<Double> MASS_RANGE_2D = Range.closed(110.35,
			190.7);

	public final static int[] TEST_CONTROL_2B = new int[] { 40, 101 };
	public final static int[] TEST_CONTROL_2C = new int[] { 50, 111 };
	public final static int[] TEST_CONTROL_2D = new int[] { 50, 191 };

	@Test
	public void testDetermineMassRangeFromData_SingleFile() {

		@SuppressWarnings("unchecked")
		RawDataFile file = mockRawDataFile(new int[] { 0 }, MASS_RANGE_1);

		int[] massRange = PeakInvestigatorParameters
				.determineMassRangeFromData(new RawDataFile[] { file });

		assertArrayEquals(TEST_CONTROL_1, massRange);
	}

	/**
	 * Tests the following possible mass ranges:
	 * 
	 * <ul>
	 * <li>Scan 1 --+---------+--
	 * <li>Scan 2 -----+--+------
	 * </ul>
	 */
	@Test
	public void testDetermineMassRangeFromData_TwoFilesA() {

		@SuppressWarnings("unchecked")
		RawDataFile file = mockRawDataFile(new int[] { 0, 1 }, MASS_RANGE_1,
				MASS_RANGE_2A);

		int[] massRange = PeakInvestigatorParameters
				.determineMassRangeFromData(new RawDataFile[] { file });

		assertArrayEquals(TEST_CONTROL_1, massRange);
	}

	/**
	 * Tests the following possible mass ranges:
	 * 
	 * <ul>
	 * <li>Scan 1 ---+------+--
	 * <li>Scan 2 -+----+------
	 * </ul>
	 */
	@Test
	public void testDetermineMassRangeFromData_TwoFilesB() {

		@SuppressWarnings("unchecked")
		RawDataFile file = mockRawDataFile(new int[] { 0, 1 }, MASS_RANGE_1,
				MASS_RANGE_2B);

		int[] massRange = PeakInvestigatorParameters
				.determineMassRangeFromData(new RawDataFile[] { file });

		assertArrayEquals(TEST_CONTROL_2B, massRange);
	}

	/**
	 * Tests the following possible mass ranges:
	 * 
	 * <ul>
	 * <li>Scan 1 --+-----+----
	 * <li>Scan 2 ----+-----+--
	 * </ul>
	 */
	@Test
	public void testDetermineMassRangeFromData_TwoFilesC() {

		@SuppressWarnings("unchecked")
		RawDataFile file = mockRawDataFile(new int[] { 0, 1 }, MASS_RANGE_1,
				MASS_RANGE_2C);

		int[] massRange = PeakInvestigatorParameters
				.determineMassRangeFromData(new RawDataFile[] { file });

		assertArrayEquals(TEST_CONTROL_2C, massRange);
	}

	/**
	 * Tests the following possible mass ranges:
	 * 
	 * <ul>
	 * <li>Scan 1 --+--+---------
	 * <li>Scan 2 ---------+--+--
	 * </ul>
	 */
	@Test
	public void testDetermineMassRangeFromData_TwoFilesD() {

		@SuppressWarnings("unchecked")
		RawDataFile file = mockRawDataFile(new int[] { 0, 1 }, MASS_RANGE_1,
				MASS_RANGE_2D);

		int[] massRange = PeakInvestigatorParameters
				.determineMassRangeFromData(new RawDataFile[] { file });

		assertArrayEquals(TEST_CONTROL_2D, massRange);
	}

	@SuppressWarnings("unchecked")
	private RawDataFile mockRawDataFile(int[] scanNumbers,
			Range<Double>... ranges) {
		if (scanNumbers.length != ranges.length) {
			throw new IllegalArgumentException(
					"Numbers of scan and ranges don't agree.");
		}

		Scan[] scans = new Scan[ranges.length];
		for (int i = 0; i < ranges.length; i++) {
			Scan scan = mock(Scan.class);
			when(scan.getDataPointMZRange()).thenReturn(ranges[i]);
			scans[i] = scan;
		}

		RawDataFile file = mock(RawDataFile.class);
		when(file.getScanNumbers()).thenReturn(scanNumbers);
		for (int i = 0; i < scans.length; i++) {
			when(file.getScan(i)).thenReturn(scans[i]);
		}

		return file;
	}

	/**
	 * Used for specifying behavior of Mocked PeakInvestigatorSaaS.
	 */
	private class IsPiVersionsAction extends ArgumentMatcher<PiVersionsAction> {

		@Override
		public boolean matches(Object argument) {
			if (argument instanceof PiVersionsAction) {
				return true;
			}

			return false;
		}
	}

	private class IsWindowOrNull extends ArgumentMatcher<Window> {

		@Override
		public boolean matches(Object argument) {
			if (argument instanceof Window) {
				return true;
			}

			if (argument == null) {
				return true;
			}

			return false;
		}
	}
}
