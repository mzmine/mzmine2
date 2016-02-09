package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator;

import static org.junit.Assert.*;

import java.awt.Window;

import net.sf.mzmine.desktop.preferences.MZminePreferences;
import net.sf.mzmine.util.ExitCode;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;

import static org.mockito.Mockito.*;

import com.veritomyx.PeakInvestigatorSaaS;
import com.veritomyx.VeritomyxSettings;
import com.veritomyx.actions.BaseAction.ResponseFormatException;
import com.veritomyx.actions.BaseAction;
import com.veritomyx.actions.PiVersionsAction;

public class PeakInvestigatorParametersTest {

	public final static String VERSIONS_RESPONSE_1 = "{\"Action\":\"PI_VERSIONS\",\"Current\":\"1.2\",\"LastUsed\":\"\",\"Count\":3,\"Versions\":[\"1.2\",\"1.1\",\"1.0.0\"]}";
	public final static String VERSIONS_RESPONSE_2 = "{\"Action\":\"PI_VERSIONS\",\"Current\":\"1.2\",\"LastUsed\":\"1.0.0\",\"Count\":3,\"Versions\":[\"1.2\",\"1.1\",\"1.0.0\"]}";
	public final static String VERSIONS_RESPONSE_3 = "{\"Action\":\"PI_VERSIONS\",\"Current\":\"1.2\",\"LastUsed\":\"1.2\",\"Count\":3,\"Versions\":[\"1.2\",\"1.1\",\"1.0.0\"]}";

	@Test
	public void testFormatPiVersionsEmptyLastUsed()
			throws ResponseFormatException {
		PiVersionsAction action = new PiVersionsAction("server", "username",
				"password");
		action.processResponse(VERSIONS_RESPONSE_1);
		String[] choices = PeakInvestigatorParameters.formatPiVersions(action);
		assertArrayEquals(new String[] { "1.2 (current)", "1.1", "1.0.0" },
				choices);
	}

	@Test
	public void testFormatPiVersionsLastUsedOld()
			throws ResponseFormatException {
		PiVersionsAction action = new PiVersionsAction("server", "username",
				"password");
		action.processResponse(VERSIONS_RESPONSE_2);
		String[] choices = PeakInvestigatorParameters.formatPiVersions(action);
		assertArrayEquals(new String[] { "1.2 (current)", "1.1",
				"1.0.0 (last used)" }, choices);
	}

	@Test
	public void testFormatPiVersionsLastUsedCurrent()
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

		VeritomyxSettings settings = new VeritomyxSettings("test", "username",
				"password", 0);
		MZminePreferences preferences = mock(MZminePreferences.class);
		when(preferences.getVeritomyxSettings()).thenReturn(settings);

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

		// mock preferences, and return Cancel exitcode from dialog
		MZminePreferences preferences = mock(MZminePreferences.class);
		when(preferences.getVeritomyxSettings()).thenReturn(settings);
		when(preferences.showSetupDialog(argThat(new IsWindowOrNull()), anyBoolean()))
				.thenReturn(ExitCode.CANCEL);

		// mock PI service, and then return error due to bad credentials
		PeakInvestigatorSaaS service = mock(PeakInvestigatorSaaS.class);
		String response = BaseAction.ERROR_CREDENTIALS.replace("ACTION", "PI_VERSIONS");
		when(service.executeAction(argThat(new IsPiVersionsAction())))
				.thenReturn(response);

		PeakInvestigatorParameters.setDialogFactory(new EmptyDialogFactory());
		PiVersionsAction action = PeakInvestigatorParameters
				.performPiVersionsCall(preferences, service, null);

		assertEquals(null, action);
	}

	@Test
	public void testPerformPiVersionCall_BadOnFirstTryThenGood()
			throws ResponseFormatException {

		VeritomyxSettings settings = new VeritomyxSettings("test", "username",
				"password", 0);

		// mock preferences, and return Cancel exitcode from dialog
		MZminePreferences preferences = mock(MZminePreferences.class);
		when(preferences.getVeritomyxSettings()).thenReturn(settings);
		when(preferences.showSetupDialog(argThat(new IsWindowOrNull()), anyBoolean()))
				.thenReturn(ExitCode.OK);

		// mock PI service, and then return error due to bad credentials
		PeakInvestigatorSaaS service = mock(PeakInvestigatorSaaS.class);
		String response = BaseAction.ERROR_CREDENTIALS.replace("ACTION", "PI_VERSIONS");
		when(service.executeAction(argThat(new IsPiVersionsAction())))
				.thenReturn(response, VERSIONS_RESPONSE_1);

		PeakInvestigatorParameters.setDialogFactory(new EmptyDialogFactory());
		PiVersionsAction action = PeakInvestigatorParameters
				.performPiVersionsCall(preferences, service, null);

		assertFalse(action.hasError());
	}

	private class EmptyDialogFactory extends AbstractTestDialogFactory {

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
