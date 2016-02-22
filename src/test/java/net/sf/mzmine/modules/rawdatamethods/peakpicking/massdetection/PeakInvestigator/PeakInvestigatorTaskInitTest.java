package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator;

import static org.junit.Assert.*;
import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.datamodel.Scan;
import net.sf.mzmine.datamodel.impl.RemoteJob;
import net.sf.mzmine.datamodel.impl.SimpleScan;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.PeakInvestigatorTask.ResponseErrorException;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs.InitDialog;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs.PeakInvestigatorDialogFactory;
import net.sf.mzmine.project.impl.RawDataFileImpl;
import net.sf.mzmine.util.ExitCode;
import net.sf.mzmine.util.dialogs.HeadlessDialogFactory;
import net.sf.mzmine.util.dialogs.HeadlessBasicDialog;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.veritomyx.PeakInvestigatorSaaS;
import com.veritomyx.actions.BaseAction;
import com.veritomyx.actions.StatusAction;
import com.veritomyx.actions.BaseAction.ResponseFormatException;
import com.veritomyx.actions.InitAction;

public class PeakInvestigatorTaskInitTest {

	@Rule public ExpectedException thrown = ExpectedException.none();

	/**
	 * Test PeakInvestigatorTask.initialize() with real response and OK click on
	 * dialog.
	 */
	@Test
	public void testInitialize_SubmitOk() throws IllegalStateException,
			ResponseFormatException, ResponseErrorException, JSchException {

		PeakInvestigatorTask task = createDefaultSubmitTask(
				InitAction.EXAMPLE_RESPONSE_1).usingDialogFactory(
				new EmptyOkDialogFactory());
		task.initializeSubmit("1.2", 2, new int[] { 50, 500 }, "job-blah");

		assertEquals("V-504.1551", task.getName());
	}

	/**
	 * Test PeakInvestigatorTask.initialize() with real response and Cancel
	 * click on dialog.
	 */
	@Test
	public void testInitialize_SubmitCancel() throws IllegalStateException,
			ResponseFormatException, ResponseErrorException, JSchException {

		PeakInvestigatorTask task = createDefaultSubmitTask(
				InitAction.EXAMPLE_RESPONSE_1).usingDialogFactory(
				new EmptyCancelDialogFactory());
		task.initializeSubmit("1.2", 2, new int[] { 50, 500 }, "job-blah");

		assertEquals(null, task.getName());
	}

	/**
	 * Test PeakInvestigatorTask.initialize() with HTML response.
	 */
	@Test
	public void testInitialize_SubmitResponseHTML()
			throws IllegalStateException, ResponseFormatException,
			ResponseErrorException, JSchException {

		thrown.expect(ResponseFormatException.class);
		thrown.expectMessage("Server response appears to be HTML/XML");

		PeakInvestigatorTask task = createDefaultSubmitTask(BaseAction.API_SOURCE)
				.usingDialogFactory(new EmptyOkDialogFactory());
		task.initializeSubmit("1.2", 2, new int[] { 50, 500 }, "job-blah");

		fail("Should not reach here.");
	}

	/**
	 * Test PeakInvestigatorTask.initialize() with real ERROR response.
	 */
	@Test
	public void testInitialize_SubmitResponseError()
			throws IllegalStateException, ResponseFormatException,
			ResponseErrorException, JSchException {

		thrown.expect(ResponseErrorException.class);
		thrown.expectMessage("Invalid username or password");

		PeakInvestigatorDialogFactory factory = new EmptyOkDialogFactory();
		String response = BaseAction.ERROR_CREDENTIALS
				.replace("ACTION", "INIT");

		PeakInvestigatorTask task = createDefaultSubmitTask(response)
				.usingDialogFactory(factory);
		task.initializeSubmit("1.2", 2, new int[] { 50, 500 }, "job-blah");

		fail("Should not reach here.");
	}

	/**
	 * Convenience function to build PeakInvestigatorTask that has setup with
	 * PeakInvestigatorSaaS and RawDataFile mocks.
	 */
	private PeakInvestigatorTask createDefaultSubmitTask(String response) throws JSchException {
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

	private PeakInvestigatorTask createDefaultFetchTask(String response,
			ArgumentCaptor<StatusAction> action) throws JSchException {
		PeakInvestigatorSaaS vtmx = mock(PeakInvestigatorSaaS.class);
		when(vtmx.executeAction(action.capture())).thenReturn(response);

		RemoteJob job = mock(RemoteJob.class);
		RawDataFile rawFile = mock(RawDataFileImpl.class);
		when(rawFile.getJob(anyString())).thenReturn(job);

		PeakInvestigatorTask task = new PeakInvestigatorTask("test.com",
				"user", "password", 0).withService(vtmx).withRawDataFile(
				rawFile);
		return task;
	}

	@Test
	public void testInitialize_FetchRunning() throws ResponseFormatException,
			ResponseErrorException, JSchException {

		ArgumentCaptor<StatusAction> actionCaptor = ArgumentCaptor
				.forClass(StatusAction.class);
		PeakInvestigatorDialogFactory factory = new EmptyOkDialogFactory();

		PeakInvestigatorTask task = createDefaultFetchTask(
				StatusAction.EXAMPLE_RESPONSE_1, actionCaptor);
		task.usingDialogFactory(factory);
		task.initializeFetch("|job-C1.10[PI]", true);

		StatusAction action = actionCaptor.getValue();
		assertEquals(StatusAction.Status.Running, action.getStatus());
		assertNull(task.getName());

		HeadlessBasicDialog dialog = (HeadlessBasicDialog) ((HeadlessDialogFactory) factory)
				.getDialog();
		assertEquals(StatusAction.RUNNING_STRING, dialog.getInfoMessage());
	}

	@Test
	public void testInitialize_FetchDone() throws ResponseFormatException,
			ResponseErrorException, JSchException {

		ArgumentCaptor<StatusAction> actionCaptor = ArgumentCaptor
				.forClass(StatusAction.class);
		PeakInvestigatorDialogFactory factory = new EmptyOkDialogFactory();

		PeakInvestigatorTask task = createDefaultFetchTask(
				StatusAction.EXAMPLE_RESPONSE_2, actionCaptor);
		task.usingDialogFactory(factory);
		task.initializeFetch("|job-C1.10[PI]", true);

		StatusAction action = actionCaptor.getValue();
		assertEquals(StatusAction.Status.Done, action.getStatus());
		assertEquals("C1.10", task.getName());
	}

	@Test
	public void testInitialize_FetchResponseError() throws ResponseFormatException,
			ResponseErrorException, JSchException {

		thrown.expect(ResponseErrorException.class);
		thrown.expectMessage("Invalid username or password");

		ArgumentCaptor<StatusAction> actionCaptor = ArgumentCaptor
				.forClass(StatusAction.class);
		PeakInvestigatorDialogFactory factory = new EmptyOkDialogFactory();

		String response = BaseAction.ERROR_CREDENTIALS.replace("ACTION", "STATUS");
		PeakInvestigatorTask task = createDefaultFetchTask(response,
				actionCaptor);
		task.usingDialogFactory(factory);
		task.initializeFetch("|job-C1.10[PI]", true);

		fail("Should not reach here.");
	}

	@Test
	public void testInitialize_FetchResponseHTML() throws ResponseFormatException,
			ResponseErrorException, JSchException {

		thrown.expect(ResponseFormatException.class);
		thrown.expectMessage("Server response appears to be HTML/XML");

		ArgumentCaptor<StatusAction> actionCaptor = ArgumentCaptor
				.forClass(StatusAction.class);
		PeakInvestigatorDialogFactory factory = new EmptyOkDialogFactory();

		PeakInvestigatorTask task = createDefaultFetchTask(
				BaseAction.API_SOURCE, actionCaptor);
		task.usingDialogFactory(factory);
		task.initializeFetch("|job-C1.10[PI]", true);

		fail("Should not reach here.");
	}

	@Test
	public void testInitialize_FetchDeleted() throws ResponseFormatException,
			ResponseErrorException, JSchException {

		ArgumentCaptor<StatusAction> actionCaptor = ArgumentCaptor
				.forClass(StatusAction.class);
		PeakInvestigatorDialogFactory factory = new EmptyOkDialogFactory();

		PeakInvestigatorTask task = createDefaultFetchTask(
				StatusAction.EXAMPLE_RESPONSE_3, actionCaptor);
		task.usingDialogFactory(factory);
		task.initializeFetch("|job-C1.10[PI]", true);

		StatusAction action = actionCaptor.getValue();
		assertEquals(StatusAction.Status.Deleted, action.getStatus());
		assertNull(task.getName());

		HeadlessBasicDialog dialog = (HeadlessBasicDialog) ((HeadlessDialogFactory) factory)
				.getDialog();
		assertEquals(StatusAction.DELETED_STRING, dialog.getInfoMessage());
	}

	/**
	 * DialogFactory that creates an InitDialog that simulates OK button click and standard
	 * Error Dialog for tracking error messages.
	 */
	private class EmptyOkDialogFactory extends HeadlessDialogFactory implements PeakInvestigatorDialogFactory {

		@Override
		public InitDialog createInitDialog(String version, InitAction action) {
			return new EmptyOkInitDialog();
		}

		@Override
		public SftpProgressMonitor createSftpProgressMonitor() {
			return null;
		}
	}

	/**
	 * DialogFactory that creates an InitDialog that simulates Cancel button
	 * click and standard Error Dialog for tracking error messages.
	 */
	private class EmptyCancelDialogFactory extends HeadlessDialogFactory implements PeakInvestigatorDialogFactory {

		@Override
		public InitDialog createInitDialog(String version, InitAction action) {
			return new EmptyCancelInitDialog();
		}

		@Override
		public SftpProgressMonitor createSftpProgressMonitor() {
			return null;
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
