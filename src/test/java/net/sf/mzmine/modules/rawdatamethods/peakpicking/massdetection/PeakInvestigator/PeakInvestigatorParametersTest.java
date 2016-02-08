package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator;

import static org.junit.Assert.*;

import org.junit.Test;

import com.veritomyx.actions.BaseAction.ResponseFormatException;
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

}
