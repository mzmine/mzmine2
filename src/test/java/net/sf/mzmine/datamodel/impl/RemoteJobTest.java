package net.sf.mzmine.datamodel.impl;

import static org.junit.Assert.*;
import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.util.dialogs.HeadlessBasicDialog;
import net.sf.mzmine.util.dialogs.HeadlessDialogFactory;
import net.sf.mzmine.util.dialogs.interfaces.DialogFactory;

import org.junit.Test;
import org.mockito.ArgumentMatcher;

import static org.mockito.Mockito.*;

import com.veritomyx.PeakInvestigatorSaaS;
import com.veritomyx.VeritomyxSettings;
import com.veritomyx.actions.BaseAction;
import com.veritomyx.actions.BaseAction.ResponseFormatException;
import com.veritomyx.actions.StatusAction;

public class RemoteJobTest {

	@Test
	public void testGetStatus_Running() throws ResponseFormatException {
		PeakInvestigatorSaaS server = mock(PeakInvestigatorSaaS.class);
		when(server.executeAction(argThat(new IsStatusAction()))).thenReturn(
				StatusAction.EXAMPLE_RESPONSE_1);

		VeritomyxSettings settings = mock(VeritomyxSettings.class);
		RawDataFile raw = mock(RawDataFile.class);

		RemoteJob job = new RemoteJob("job-test", raw, "job-test", server);

		HeadlessDialogFactory factory = new HeadlessDialogFactory();
		job.setDialogFactory(factory);

		int retval = job.getStatus(server, settings);

		assertEquals(RemoteJob.Status.RUNNING.ordinal(), retval);

		HeadlessBasicDialog dialog = (HeadlessBasicDialog) factory.getDialog();
		assertEquals(StatusAction.RUNNING_STRING, dialog.getInfoMessage());
	}

	@Test
	public void testGetStatus_Done() throws ResponseFormatException {
		PeakInvestigatorSaaS server = mock(PeakInvestigatorSaaS.class);
		when(server.executeAction(argThat(new IsStatusAction()))).thenReturn(
				StatusAction.EXAMPLE_RESPONSE_2);

		VeritomyxSettings settings = mock(VeritomyxSettings.class);
		RawDataFile raw = mock(RawDataFile.class);

		RemoteJob job = new RemoteJob("job-test", raw, "job-test", server);

		HeadlessDialogFactory factory = new HeadlessDialogFactory();
		job.setDialogFactory(factory);

		int retval = job.getStatus(server, settings);

		assertEquals(RemoteJob.Status.DONE.ordinal(), retval);

		HeadlessBasicDialog dialog = (HeadlessBasicDialog) factory.getDialog();
		assertEquals(StatusAction.DONE_STRING, dialog.getInfoMessage());
	}

	@Test
	public void testGetStatus_Deleted() throws ResponseFormatException {
		PeakInvestigatorSaaS server = mock(PeakInvestigatorSaaS.class);
		when(server.executeAction(argThat(new IsStatusAction()))).thenReturn(
				StatusAction.EXAMPLE_RESPONSE_3);

		VeritomyxSettings settings = mock(VeritomyxSettings.class);
		RawDataFile raw = mock(RawDataFile.class);

		RemoteJob job = new RemoteJob("job-test", raw, "job-test", server);

		HeadlessDialogFactory factory = new HeadlessDialogFactory();
		job.setDialogFactory(factory);

		int retval = job.getStatus(server, settings);

		assertEquals(RemoteJob.Status.DELETED.ordinal(), retval);

		HeadlessBasicDialog dialog = (HeadlessBasicDialog) factory.getDialog();
		assertEquals(StatusAction.DELETED_STRING, dialog.getInfoMessage());
	}

	@Test
	public void testGetStatus_Error() throws ResponseFormatException {
		PeakInvestigatorSaaS server = mock(PeakInvestigatorSaaS.class);
		String response = BaseAction.ERROR_CREDENTIALS.replace("ACTION",
				"STATUS");
		when(server.executeAction(argThat(new IsStatusAction()))).thenReturn(
				response);

		VeritomyxSettings settings = mock(VeritomyxSettings.class);
		RawDataFile raw = mock(RawDataFile.class);

		RemoteJob job = new RemoteJob("job-test", raw, "job-test", server);
		
		HeadlessDialogFactory factory = new HeadlessDialogFactory();
		job.setDialogFactory(factory);

		int retval = job.getStatus(server, settings);

		assertEquals(RemoteJob.Status.ERROR.ordinal(), retval);

		HeadlessBasicDialog dialog = (HeadlessBasicDialog) factory.getDialog();
		assertEquals("Invalid username or password - can not validate",
				dialog.getErrorMessage());
	}

	private class IsStatusAction extends ArgumentMatcher<StatusAction> {

		@Override
		public boolean matches(Object argument) {
			if (argument instanceof StatusAction) {
				return true;
			}

			return false;
		}

	}
}
