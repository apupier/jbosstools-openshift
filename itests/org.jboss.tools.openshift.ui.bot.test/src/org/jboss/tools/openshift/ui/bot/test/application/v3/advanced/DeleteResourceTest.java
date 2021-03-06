/*******************************************************************************
 * Copyright (c) 2007-2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v 1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.openshift.ui.bot.test.application.v3.advanced;

import static org.junit.Assert.fail;

import org.eclipse.reddeer.common.exception.WaitTimeoutExpiredException;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.junit.requirement.inject.InjectRequirement;
import org.hamcrest.core.StringStartsWith;
import org.jboss.tools.openshift.reddeer.condition.AmountOfResourcesExists;
import org.jboss.tools.openshift.reddeer.condition.OpenShiftResourceExists;
import org.jboss.tools.openshift.reddeer.enums.Resource;
import org.jboss.tools.openshift.reddeer.enums.ResourceState;
import org.jboss.tools.openshift.reddeer.requirement.OpenShiftCommandLineToolsRequirement.OCBinary;
import org.jboss.tools.openshift.reddeer.requirement.OpenShiftConnectionRequirement.RequiredBasicConnection;
import org.jboss.tools.openshift.reddeer.requirement.OpenShiftProjectRequirement;
import org.jboss.tools.openshift.reddeer.requirement.OpenShiftProjectRequirement.RequiredProject;
import org.jboss.tools.openshift.reddeer.requirement.OpenShiftServiceRequirement.RequiredService;
import org.jboss.tools.openshift.reddeer.view.OpenShiftExplorerView;
import org.jboss.tools.openshift.reddeer.view.resources.OpenShiftResource;
import org.jboss.tools.openshift.ui.bot.test.common.OpenShiftUtils;
import org.junit.BeforeClass;
import org.junit.Test;

@OCBinary
@RequiredBasicConnection
@RequiredProject
@RequiredService(service = "eap-app", template = "resources/eap70-basic-s2i-helloworld.json")
public class DeleteResourceTest {

	@InjectRequirement
	private static OpenShiftProjectRequirement projectReq;

	@BeforeClass
	public static void waitForApplication() {
		new WaitUntil(new OpenShiftResourceExists(Resource.BUILD, "eap-app-1", ResourceState.COMPLETE,
				projectReq.getProjectName()), TimePeriod.getCustom(600), true);

		new WaitUntil(new AmountOfResourcesExists(Resource.POD, 2, projectReq.getProjectName()), TimePeriod.VERY_LONG,
				true);
	}

	@Test
	public void testDeletePod() {
		OpenShiftResource applicationPod = OpenShiftUtils.getOpenShiftPod(projectReq.getProjectName(),new StringStartsWith("eap-app-"));
		String podName = applicationPod.getName();

		applicationPod.delete();

		try {
			OpenShiftResourceExists openShiftResourceExists = new OpenShiftResourceExists(Resource.POD, podName,
					ResourceState.UNSPECIFIED, projectReq.getProjectName());
			new WaitWhile(openShiftResourceExists, TimePeriod.getCustom(15));
		} catch (WaitTimeoutExpiredException ex) {
			fail("Application pod should be deleted at this point, but it it still present.");
		}
	}

	@Test
	public void testDeleteBuild() {
		deleteResourceAndAssert(Resource.BUILD);
	}

	@Test
	public void testDeleteBuildConfig() {
		deleteResourceAndAssert(Resource.BUILD_CONFIG);
	}

	@Test
	public void testDeleteRoute() {
		deleteResourceAndAssert(Resource.ROUTE);
	}

	@Test
	public void testDeleteImageStream() {
		deleteResourceAndAssert(Resource.IMAGE_STREAM);
	}

	@Test
	public void testDeleteDeploymentConfig() {
		deleteResourceAndAssert(Resource.DEPLOYMENT_CONFIG);
	}

	@Test
	public void testDeleteService() {
		deleteResourceAndAssert(Resource.SERVICE);
	}

	private void deleteResourceAndAssert(Resource resource) {
		OpenShiftExplorerView explorer = new OpenShiftExplorerView();
		OpenShiftResource rsrc = explorer.getOpenShift3Connection().getProject(projectReq.getProjectName())
				.getOpenShiftResources(resource).get(0);
		String resourceName = rsrc.getName();
		rsrc.delete();

		try {
			OpenShiftResourceExists openShiftResourceExists = new OpenShiftResourceExists(resource, resourceName,
					ResourceState.UNSPECIFIED, projectReq.getProjectName());
			new WaitWhile(openShiftResourceExists, TimePeriod.getCustom(15));
		} catch (WaitTimeoutExpiredException ex) {
			fail("Route " + resource + " should be deleted at this point but it is still present.");
		}
	}
}
