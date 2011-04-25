/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.region.tests.system;

import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;
import org.eclipse.equinox.region.Region;
import org.eclipse.equinox.region.RegionDigraph;
import org.eclipse.equinox.region.tests.BundleInstaller;
import org.osgi.framework.*;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;

/*
 * Here are the dependencies between the bundles:
 * PP1 --> NONE
 * CP1 --> NONE
 * SP1 -- package pkg1.* --> PP1
 * PP2 -- capability CP1 --> CP1
 * SP2 -- package pkg2.* --> PP2
 * CP2 -- package pkg1.* --> PP1
 * CP2 -- service pkg1.* --> SP1
 * PC1 -- package pkg2.* --> PP2
 * BC1 -- bundle PP2     --> PP2
 * SC1 -- service pkg2.* --> SP2
 * SC1 -- package pkg2.* --> PP2
 * CC1 -- capability CP2 --> CP2
 */
public class AbstractRegionSystemTest extends TestCase {
	public static final String PP1 = "PackageProvider1";
	public static final String SP1 = "ServiceProvider1";
	public static final String CP1 = "CapabilityProvider1";
	public static final String PP2 = "PackageProvider2";
	public static final String SP2 = "ServiceProvider2";
	public static final String CP2 = "CapabilityProvider2";
	public static final String PC1 = "PackageClient1";
	public static final String BC1 = "BundleClient1";
	public static final String SC1 = "ServiceClient1";
	public static final String CC1 = "CapabilityClient1";
	public static List<String> ALL = Arrays.asList(PP1, SP1, CP1, PP2, SP2, CP2, PC1, BC1, SC1, CC1);

	protected BundleInstaller bundleInstaller;
	protected Bundle regionBundle;
	protected RegionDigraph digraph;
	ServiceReference<RegionDigraph> digraphReference;

	@Override
	protected void setUp() throws Exception {
		// this is a fragment of the region impl bundle
		// this line makes sure the region impl bundle is started
		regionBundle = FrameworkUtil.getBundle(this.getClass());

		startRegionBundle();

		// fun code to get our fragment bundle
		Bundle testBundle = regionBundle.adapt(BundleWiring.class).getProvidedWires(BundleRevision.HOST_NAMESPACE).get(0).getRequirerWiring().getBundle();
		bundleInstaller = new BundleInstaller("bundle_tests", regionBundle, testBundle); //$NON-NLS-1$
	}

	protected void startRegionBundle() throws BundleException {
		regionBundle.start();
		BundleContext context = regionBundle.getBundleContext();
		assertNotNull("No context found", context);

		digraphReference = context.getServiceReference(RegionDigraph.class);
		assertNotNull("No digraph found", digraphReference);
		digraph = context.getService(digraphReference);
		assertNotNull("No digraph found");
	}

	@Override
	protected void tearDown() throws Exception {
		if ((regionBundle.getState() & Bundle.ACTIVE) == 0)
			startRegionBundle();
		for (Region region : digraph) {
			if (!region.contains(0)) {
				digraph.removeRegion(region);
			}
		}
		bundleInstaller.shutdown();
		if (digraphReference != null)
			regionBundle.getBundleContext().ungetService(digraphReference);
	}

	protected BundleContext getContext() {
		BundleContext context = regionBundle.getBundleContext();
		assertNotNull("No context available", context);
		return context;
	}
}