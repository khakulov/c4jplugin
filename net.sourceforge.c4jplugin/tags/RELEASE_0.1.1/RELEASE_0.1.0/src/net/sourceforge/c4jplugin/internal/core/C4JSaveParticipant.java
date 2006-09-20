package net.sourceforge.c4jplugin.internal.core;

import java.io.File;
import java.io.IOException;

import net.sourceforge.c4jplugin.C4JActivator;
import net.sourceforge.c4jplugin.internal.ui.text.UIMessages;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

public class C4JSaveParticipant implements ISaveParticipant {

	public static final String SAVE_FILENAME = "contracts_map";
	
	public void doneSaving(ISaveContext context) {
		// delete the old saved state since it is not necessary anymore
        int previousSaveNumber = context.getPreviousSaveNumber();
        File f = getSaveFilePath(previousSaveNumber).toFile();
        f.delete();
	}

	public void prepareToSave(ISaveContext context) throws CoreException {
		
	}

	public void rollback(ISaveContext context) {
		// since the save operation has failed, delete the saved state we have just written
        int saveNumber = context.getSaveNumber();
        File f = getSaveFilePath(saveNumber).toFile();
        f.delete();
	}

	public void saving(ISaveContext context) throws CoreException {
		switch (context.getKind()) {
        case ISaveContext.FULL_SAVE:
        case ISaveContext.PROJECT_SAVE:
           // save the plug-in state
           int saveNumber = context.getSaveNumber();
           IPath saveFileName = getSaveFileName(saveNumber);
           IPath saveFilePath = getSaveFilePath(saveNumber);
           // if we fail to write, an exception is thrown and we do not update the path
           try {
				C4JActivator.getDefault().writeState(saveFilePath.toFile());
				context.map(new Path(SAVE_FILENAME), saveFileName);
				context.needSaveNumber();
			} catch (IOException e) {
				C4JActivator.getDefault().getLog().log(
						new Status(IStatus.ERROR, C4JActivator.PLUGIN_ID, 
								IStatus.OK, UIMessages.LogMessage_writingStateFailed, e));
			}
           break;
     }
	}
	
	static private IPath getSaveFilePath(int saveNumber) {
		return C4JActivator.getDefault().getStateLocation().append(getSaveFileName(saveNumber));
	}
	
	static private IPath getSaveFileName(int saveNumber) {
		String fileName = SAVE_FILENAME + "-" + saveNumber + ".xml";
		return new Path(fileName);
	}

}
