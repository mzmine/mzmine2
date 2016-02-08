package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.datamodel.Scan;
import net.sf.mzmine.datamodel.impl.SimpleScan;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs.ErrorDialog;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs.InitDialog;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs.PeakInvestigatorDialogFactory;
import net.sf.mzmine.project.impl.RawDataFileImpl;
import net.sf.mzmine.util.ExitCode;

import org.junit.Test;
import org.mockito.ArgumentMatcher;

import static org.mockito.Mockito.*;

import com.veritomyx.PeakInvestigatorSaaS;
import com.veritomyx.actions.BaseAction;
import com.veritomyx.actions.BaseAction.ResponseFormatException;
import com.veritomyx.actions.InitAction;

public class PeakInvestigatorTaskInitTest {

	private final static String API_SOURCE = "<html><head>\n"
			+ "<!--\n"
			+ "// ===================================================================\n"
			+ "// Veritomyx";

	/**
	 * Test PeakInvestigatorTask.initialize() with real response and OK click on
	 * dialog.
	 */
	@Test
	public void testInitializeOk() throws IllegalStateException,
			ResponseFormatException {

		PeakInvestigatorTask task = createDefaultTask(
				InitAction.EXAMPLE_RESPONSE_1).usingDialogFactory(
				new EmptyOkDialogFactory());
		task.initialize("1.2", 2, new int[] { 50, 500 }, "job-blah");

		assertEquals("V-504.1461", task.getName());
	}

	/**
	 * Test PeakInvestigatorTask.initialize() with real response and Cancel
	 * click on dialog.
	 */
	@Test
	public void testInitializeCancel() throws IllegalStateException,
			ResponseFormatException {

		PeakInvestigatorTask task = createDefaultTask(
				InitAction.EXAMPLE_RESPONSE_1).usingDialogFactory(
				new EmptyCancelDialogFactory());
		task.initialize("1.2", 2, new int[] { 50, 500 }, "job-blah");

		assertEquals(null, task.getName());
	}

	/**
	 * Test PeakInvestigatorTask.initialize() with HTML response.
	 */
	@Test(expected = ResponseFormatException.class)
	public void testInitializeResponseHTML() throws IllegalStateException,
			ResponseFormatException {
		PeakInvestigatorTask task = createDefaultTask(API_SOURCE)
				.usingDialogFactory(new EmptyOkDialogFactory());
		task.initialize("1.2", 2, new int[] { 50, 500 }, "job-blah");

		fail("Should not reach here.");
	}

	/**
	 * Test PeakInvestigatorTask.initialize() with real ERROR response.
	 */
	@Test
	public void testInitializeResponseError() throws IllegalStateException,
			ResponseFormatException {
		PeakInvestigatorDialogFactory factory = new EmptyOkDialogFactory();
		PeakInvestigatorTask task = createDefaultTask(
				BaseAction.ERROR_CREDENTIALS).usingDialogFactory(factory);
		task.initialize("1.2", 2, new int[] { 50, 500 }, "job-blah");

		EmptyErrorDialog dialog = (EmptyErrorDialog) ((EmptyOkDialogFactory) factory)
				.getDialog();
		assertEquals("Invalid username or password - can not validate",
				dialog.getMessage());
	}

	/**
	 * Convenience function to build PeakInvestigatorTask that has setup with
	 * PeakInvestigatorSaaS and RawDataFile mocks.
	 */
	private PeakInvestigatorTask createDefaultTask(String response) {
		PeakInvestigatorSaaS vtmx = mock(PeakInvestigatorSaaS.class);
		when(vtmx.executeAction(argThat(new IsBaseAction()))).thenReturn(
				response);

		RawDataFile rawFile = mock(RawDataFileImpl.class);
		Scan scan = mock(SimpleScan.class);

		when(rawFile.getScanNumbers()).thenReturn(new int[] { 0, 1 });
		when(rawFile.getScan(anyInt())).thenReturn(scan);
		when(scan.getNumberOfDataPoints()).thenReturn(12345);

		PeakInvestigatorTask task = new PeakInvestigatorTask("test.com",
				"user", "password", 0).withService(vtmx).withRawDataFile(
				rawFile);
		return task;
	}

	/**
	 * DialogFactory that creates an InitDialog that simulates OK button click and standard
	 * Error Dialog for tracking error messages.
	 */
	private class EmptyOkDialogFactory implements PeakInvestigatorDialogFactory {

		private ErrorDialog dialog = null;

		@Override
		public InitDialog createInitDialog(String version, InitAction action) {
			return new EmptyOkInitDialog();
		}

		@Override
		public ErrorDialog createErrorDialog() {
			dialog = new EmptyErrorDialog();
			return dialog;
		}

		public ErrorDialog getDialog() {
			return dialog;
		}
	}

	/**
	 * DialogFactory that creates an InitDialog that simulates Cancel button
	 * click and standard Error Dialog for tracking error messages.
	 */
	private class EmptyCancelDialogFactory implements
			PeakInvestigatorDialogFactory {

		@Override
		public InitDialog createInitDialog(String version, InitAction action) {
			return new EmptyCancelInitDialog();
		}

		@Override
		public ErrorDialog createErrorDialog() {
			return new EmptyErrorDialog();
		}
	}

	private abstract class AbstractInitDialog implements InitDialog {

		@Override
		public ExitCode getExitCode() {
			return null;
		}

		@Override
		public String getSelectedRTO() {
			return "RTO-24";
		}

		@Override
		public void setVisible(boolean visibility) {
			// Doesn't do anything
		}
	}

	private class EmptyOkInitDialog extends AbstractInitDialog {

		@Override
		public ExitCode getExitCode() {
			return ExitCode.OK;
		}
	}

	private class EmptyCancelInitDialog extends AbstractInitDialog {

		@Override
		public ExitCode getExitCode() {
			return ExitCode.CANCEL;
		}
	}

	/**
	 * ErrorDialog implementation that keeps track of error messages.
	 */
	private class EmptyErrorDialog implements ErrorDialog {

		private String message = null;

		@Override
		public void displayMessage(String message, Logger logger) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}
	}

	/**
	 * Used for specifying behavior of Mocked PeakInvestigatorSaaS.
	 */
	private class IsBaseAction extends ArgumentMatcher<BaseAction> {

		@Override
		public boolean matches(Object argument) {
			if (argument instanceof BaseAction) {
				return true;
			}

			return false;
		}
	}

}
