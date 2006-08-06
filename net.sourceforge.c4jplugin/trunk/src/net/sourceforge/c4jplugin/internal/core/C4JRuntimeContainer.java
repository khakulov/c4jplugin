package net.sourceforge.c4jplugin.internal.core;

import java.io.IOException;
import java.net.URL;

import net.sourceforge.c4jplugin.C4JActivator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;

public class C4JRuntimeContainer implements IClasspathContainer {

	private IClasspathEntry[] fClasspathEntries;

	private static String c4jrtPath = null;

	public IClasspathEntry[] getClasspathEntries() {
		if (fClasspathEntries == null) {
			String path = getC4JRtClasspath();
			fClasspathEntries = new IClasspathEntry[1];
			IPath p = new Path(path);
			fClasspathEntries[0] = JavaCore.newLibraryEntry(p, null, null, false);
		}
		return fClasspathEntries;
	}

	public String getDescription() {
		return CoreMessages.c4jRuntimeContainerName;
	}

	public int getKind() {
		return IClasspathContainer.K_APPLICATION;
	}

	public IPath getPath() {
		return new Path(C4JActivator.C4JRT_CONTAINER);
	}

	/**
	 * Get the c4j.jar classpath entry. This is usually in
	 * plugins/net.sourceforge.c4jplugin.runtime_ <VERSION>/c4j.jar
	 */
	public static String getC4JRtClasspath() {
		if (c4jrtPath == null) {
			Bundle runtime = Platform
					.getBundle(C4JActivator.RUNTIME_PLUGIN_ID);
			if (runtime != null) {
				URL installLoc = runtime.getEntry("c4j.jar"); //$NON-NLS-1$
				if (installLoc == null) {
					// maybe it's a JARed bundle
					IPath path = new Path(runtime.getLocation().split("@")[1]); //$NON-NLS-1$
					IPath full = new Path(Platform.getInstallLocation()
							.getURL().getFile()).append(path);
					c4jrtPath = full.toString();
				} else {
					try {
						c4jrtPath = FileLocator.resolve(installLoc)
								.getFile();
					} catch (IOException e) {
					}
				}
			}
		}
		return c4jrtPath;
	}

}
