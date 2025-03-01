/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ambari.server.controller.internal;

import static org.easymock.EasyMock.createMock;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.ambari.server.controller.AmbariManagementController;
import org.apache.ambari.server.controller.ClusterRequest;
import org.apache.ambari.server.controller.ConfigurationRequest;
import org.apache.ambari.server.controller.GroupRequest;
import org.apache.ambari.server.controller.HostRequest;
import org.apache.ambari.server.controller.MemberRequest;
import org.apache.ambari.server.controller.RequestStatusResponse;
import org.apache.ambari.server.controller.ServiceComponentHostRequest;
import org.apache.ambari.server.controller.StackConfigurationRequest;
import org.apache.ambari.server.controller.TaskStatusRequest;
import org.apache.ambari.server.controller.UserRequest;
import org.apache.ambari.server.controller.predicate.AlwaysPredicate;
import org.apache.ambari.server.controller.spi.NoSuchParentResourceException;
import org.apache.ambari.server.controller.spi.NoSuchResourceException;
import org.apache.ambari.server.controller.spi.Predicate;
import org.apache.ambari.server.controller.spi.Request;
import org.apache.ambari.server.controller.spi.RequestStatus;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.spi.ResourceAlreadyExistsException;
import org.apache.ambari.server.controller.spi.SystemException;
import org.apache.ambari.server.controller.spi.UnsupportedPropertyException;
import org.apache.ambari.server.controller.utilities.PredicateBuilder;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Assert;
import org.junit.Test;

/**
 * Resource provider tests.
 */
public class AbstractResourceProviderTest {

  @Test
  public void testCheckPropertyIds() {
    Set<String> propertyIds = new HashSet<String>();
    propertyIds.add("foo");
    propertyIds.add("cat1/foo");
    propertyIds.add("cat2/bar");
    propertyIds.add("cat2/baz");
    propertyIds.add("cat3/sub1/bam");
    propertyIds.add("cat4/sub2/sub3/bat");
    propertyIds.add("cat5/subcat5/map");

    Map<Resource.Type, String> keyPropertyIds = new HashMap<Resource.Type, String>();

    AmbariManagementController managementController = createMock(AmbariManagementController.class);

    AbstractResourceProvider provider = new ServiceResourceProvider(
            propertyIds,
            keyPropertyIds,
            managementController);

    Set<String> unsupported = provider.checkPropertyIds(Collections.singleton("foo"));
    Assert.assertTrue(unsupported.isEmpty());

    // note that key is not in the set of known property ids.  We allow it if its parent is a known property.
    // this allows for Map type properties where we want to treat the entries as individual properties
    Assert.assertTrue(provider.checkPropertyIds(Collections.singleton("cat5/subcat5/map/key")).isEmpty());

    unsupported = provider.checkPropertyIds(Collections.singleton("bar"));
    Assert.assertEquals(1, unsupported.size());
    Assert.assertTrue(unsupported.contains("bar"));

    unsupported = provider.checkPropertyIds(Collections.singleton("cat1/foo"));
    Assert.assertTrue(unsupported.isEmpty());

    unsupported = provider.checkPropertyIds(Collections.singleton("cat1"));
    Assert.assertTrue(unsupported.isEmpty());
  }

  @Test
  public void testGetPropertyIds() {
    Set<String> propertyIds = new HashSet<String>();
    propertyIds.add("p1");
    propertyIds.add("foo");
    propertyIds.add("cat1/foo");
    propertyIds.add("cat2/bar");
    propertyIds.add("cat2/baz");
    propertyIds.add("cat3/sub1/bam");
    propertyIds.add("cat4/sub2/sub3/bat");

    Map<Resource.Type, String> keyPropertyIds = new HashMap<Resource.Type, String>();

    AmbariManagementController managementController = createMock(AmbariManagementController.class);

    AbstractResourceProvider provider = new ServiceResourceProvider(
            propertyIds,
            keyPropertyIds,
            managementController);

    Set<String> supportedPropertyIds = provider.getPropertyIds();
    Assert.assertTrue(supportedPropertyIds.containsAll(propertyIds));
  }

  @Test
  public void testGetRequestStatus() {
    Set<String> propertyIds = new HashSet<String>();
    Map<Resource.Type, String> keyPropertyIds = new HashMap<Resource.Type, String>();
    AmbariManagementController managementController = createMock(AmbariManagementController.class);

    AbstractResourceProvider provider = new ServiceResourceProvider(
            propertyIds,
            keyPropertyIds,
            managementController);

    RequestStatus status = provider.getRequestStatus(null);

    Assert.assertNull(status.getRequestResource());
    Assert.assertEquals(Collections.emptySet(), status.getAssociatedResources());

    RequestStatusResponse response = new RequestStatusResponse(99L);

    status = provider.getRequestStatus(response);
    Resource resource = status.getRequestResource();

    Assert.assertEquals(99L, resource.getPropertyValue("Requests/id"));
    Assert.assertEquals(Collections.emptySet(), status.getAssociatedResources());


    status = provider.getRequestStatus(response, null);
    resource = status.getRequestResource();

    Assert.assertEquals(99L, resource.getPropertyValue("Requests/id"));
    Assert.assertEquals(Collections.emptySet(), status.getAssociatedResources());


    Resource associatedResource = new ResourceImpl(Resource.Type.Service);

    Set<Resource> associatedResources = Collections.singleton(associatedResource);
    status = provider.getRequestStatus(response, associatedResources);
    resource = status.getRequestResource();

    Assert.assertEquals(99L, resource.getPropertyValue("Requests/id"));
    Assert.assertEquals(associatedResources, status.getAssociatedResources());
  }

  @Test
  public void testGetPropertyMaps() throws Exception {
    AbstractResourceProvider provider = new TestResourceProvider();

    Map<String, Object> updatePropertyMap = new HashMap<String, Object>();
    updatePropertyMap.put("SomeProperty", "SomeUpdateValue");
    updatePropertyMap.put("SomeOtherProperty", 99);

    // get the property map to update resource
    // where ClusterName=c1 and ResourceName=r1
    PredicateBuilder pb = new PredicateBuilder();
    Predicate predicate = pb.property("ClusterName").equals("c1").and().property("ResourceName").equals("r1").toPredicate();

    Set<Map<String, Object>> propertyMaps = provider.getPropertyMaps(updatePropertyMap, predicate);

    Assert.assertEquals(1, propertyMaps.size());

    Map<String, Object> map = propertyMaps.iterator().next();

    Assert.assertEquals(4, map.size());
    Assert.assertEquals("c1", map.get("ClusterName"));
    Assert.assertEquals("r1", map.get("ResourceName"));
    Assert.assertEquals("SomeUpdateValue", map.get("SomeProperty"));
    Assert.assertEquals(99, map.get("SomeOtherProperty"));

    // get the property maps to update resources
    // where ClusterName=c1 and (ResourceName=r1 or ResourceName=r2)
    pb = new PredicateBuilder();
    predicate = pb.property("ClusterName").equals("c1").and().
        begin().
          property("ResourceName").equals("r1").or().property("ResourceName").equals("r2").
        end().toPredicate();

    propertyMaps = provider.getPropertyMaps(updatePropertyMap, predicate);

    Assert.assertEquals(2, propertyMaps.size());

    for (Map<String, Object> map2 : propertyMaps) {
      Assert.assertEquals(4, map2.size());
      Assert.assertEquals("c1", map2.get("ClusterName"));
      Object resourceName = map2.get("ResourceName");
      Assert.assertTrue(resourceName.equals("r1") || resourceName.equals("r2"));
      Assert.assertEquals("SomeUpdateValue", map2.get("SomeProperty"));
      Assert.assertEquals(99, map2.get("SomeOtherProperty"));
    }

    // get the property maps to update all resources
    predicate = new AlwaysPredicate();

    propertyMaps = provider.getPropertyMaps(updatePropertyMap, predicate);

    Assert.assertEquals(4, propertyMaps.size());

    for (Map<String, Object> map2 : propertyMaps) {
      Assert.assertEquals(4, map2.size());
      Assert.assertEquals("c1", map2.get("ClusterName"));
      Object resourceName = map2.get("ResourceName");
      Assert.assertTrue(resourceName.equals("r1") || resourceName.equals("r2")||
          resourceName.equals("r3") || resourceName.equals("r4"));
      Assert.assertEquals("SomeUpdateValue", map2.get("SomeProperty"));
      Assert.assertEquals(99, map2.get("SomeOtherProperty"));
    }
  }


  // ----- helper methods ----------------------------------------------------

  /**
   * Equals check that accounts for nulls.
   *
   * @param left   the left object
   * @param right  the right object
   *
   * @return true if the left and right object are equal or both null
   */
  private static boolean eq(Object left, Object right) {
    return  left == null ? right == null : right != null && left.equals(right);
  }


  // ----- inner classes -----------------------------------------------------

  /**
   * Utility class for getting various AmbariManagmentController request related matchers.
   */
  public static class Matcher
  {
    public static ClusterRequest getClusterRequest(
        Long clusterId, String clusterName, String stackVersion, Set<String> hostNames)
    {
      EasyMock.reportMatcher(new ClusterRequestMatcher(clusterId, clusterName, stackVersion, hostNames));
      return null;
    }

    public static Set<ClusterRequest> getClusterRequestSet(
        Long clusterId, String clusterName, String provisioningState, String stackVersion, Set<String> hostNames)
    {
      EasyMock.reportMatcher(new ClusterRequestSetMatcher(clusterId, clusterName, provisioningState, stackVersion, hostNames));
      return null;
    }

    public static ConfigurationRequest getConfigurationRequest(
        String clusterName, String type, String tag, Map<String, String> configs, Map<String, Map<String, String>> configAttributes)
    {
      EasyMock.reportMatcher(new ConfigurationRequestMatcher(clusterName, type, tag, configs, configAttributes));
      return null;
    }

    public static Set<HostRequest> getHostRequestSet(String hostname, String clusterName,
                                                     Map<String, String> hostAttributes)
    {
      EasyMock.reportMatcher(new HostRequestSetMatcher(hostname, clusterName, hostAttributes));
      return null;
    }

    public static Set<ServiceComponentHostRequest> getHostComponentRequestSet(
        String clusterName, String serviceName, String componentName, String hostName,
        Map<String, String> configVersions, String desiredState)
    {
      EasyMock.reportMatcher(new HostComponentRequestSetMatcher(
          clusterName, serviceName, componentName, hostName, configVersions, desiredState));
      return null;
    }

    public static Set<TaskStatusRequest> getTaskRequestSet(Long requestId, Long taskId)
    {
      EasyMock.reportMatcher(new TaskRequestSetMatcher(requestId, taskId));
      return null;
    }

    public static Set<UserRequest> getUserRequestSet(String name)
    {
      EasyMock.reportMatcher(new UserRequestSetMatcher(name));
      return null;
    }

    public static Set<GroupRequest> getGroupRequestSet(String name)
    {
      EasyMock.reportMatcher(new GroupRequestSetMatcher(name));
      return null;
    }

    public static Set<MemberRequest> getMemberRequestSet(String groupname, String username)
    {
      EasyMock.reportMatcher(new MemberRequestSetMatcher(groupname, username));
      return null;
    }

    public static Set<StackConfigurationRequest> getStackConfigurationRequestSet(String stackName, String stackVersion,
        String serviceName, String propertyName)
    {
      EasyMock.reportMatcher(new StackConfigurationRequestSetMatcher(stackName, stackVersion, serviceName, propertyName));
      return null;
    }
  }

  /**
   * Matcher for a ClusterRequest.
   */
  public static class ClusterRequestMatcher extends ClusterRequest implements IArgumentMatcher {

    public ClusterRequestMatcher(Long clusterId, String clusterName, String stackVersion, Set<String> hostNames) {
      super(clusterId, clusterName, stackVersion, hostNames);
    }

    @Override
    public boolean matches(Object o) {
      return o instanceof ClusterRequest &&
          eq(((ClusterRequest) o).getClusterId(), getClusterId()) &&
          eq(((ClusterRequest) o).getClusterName(), getClusterName()) &&
          eq(((ClusterRequest) o).getStackVersion(), getStackVersion()) &&
          eq(((ClusterRequest) o).getHostNames(), getHostNames());
    }

    @Override
    public void appendTo(StringBuffer stringBuffer) {
      stringBuffer.append("ClusterRequestMatcher(").append(super.toString()).append(")");
    }
  }

  /**
   * Matcher for a ClusterRequest set.
   */
  public static class ClusterRequestSetMatcher extends ClusterRequest implements IArgumentMatcher {

    public ClusterRequestSetMatcher(Long clusterId, String clusterName, String provisioningState, String stackVersion, Set<String> hostNames) {
      super(clusterId, clusterName, provisioningState, stackVersion, hostNames);
    }

    @Override
    public boolean matches(Object o) {
      if (!(o instanceof Set)) {
        return false;
      }

      Set set = (Set) o;

      if (set.size() != 1) {
        return false;
      }

      Object request = set.iterator().next();

      return eq(((ClusterRequest) request).getClusterId(), getClusterId()) &&
          eq(((ClusterRequest) request).getClusterName(), getClusterName()) &&
          eq(((ClusterRequest) request).getStackVersion(), getStackVersion()) &&
          eq(((ClusterRequest) request).getHostNames(), getHostNames());
    }

    @Override
    public void appendTo(StringBuffer stringBuffer) {
      stringBuffer.append("ClusterRequestSetMatcher(").append(super.toString()).append(")");
    }
  }

  /**
   * Matcher for a ConfigurationRequest.
   */
  public static class ConfigurationRequestMatcher extends ConfigurationRequest implements IArgumentMatcher {

    public ConfigurationRequestMatcher(String clusterName, String type, String tag, Map<String, String> configs, Map<String, Map<String, String>> configsAttributes) {
      super(clusterName, type, tag, configs, configsAttributes);
    }

    @Override
    public boolean matches(Object o) {
      return o instanceof ConfigurationRequest &&
          eq(((ConfigurationRequest) o).getClusterName(), getClusterName()) &&
          eq(((ConfigurationRequest) o).getType(), getType()) &&
          eq(((ConfigurationRequest) o).getVersionTag(), getVersionTag()) &&
          eq(((ConfigurationRequest) o).getProperties(), getProperties()) &&
          eq(((ConfigurationRequest) o).getPropertiesAttributes(), getPropertiesAttributes());
    }

    @Override
    public void appendTo(StringBuffer stringBuffer) {
      stringBuffer.append("ConfigurationRequestMatcher(").append(super.toString()).append(")");
    }
  }


  /**
   * Matcher for a HostRequest set containing a single request.
   */
  public static class HostRequestSetMatcher extends HashSet<HostRequest> implements IArgumentMatcher {

    private final HostRequest hostRequest;

    public HostRequestSetMatcher(String hostname, String clusterName, Map<String, String> hostAttributes) {
      this.hostRequest = new HostRequest(hostname, clusterName, hostAttributes);
      add(this.hostRequest);
    }

    @Override
    public boolean matches(Object o) {
      if (!(o instanceof Set)) {
        return false;
      }

      Set set = (Set) o;

      if (set.size() != 1) {
        return false;
      }

      Object request = set.iterator().next();

      return request instanceof HostRequest &&
          eq(((HostRequest) request).getClusterName(), hostRequest.getClusterName()) &&
          eq(((HostRequest) request).getHostname(), hostRequest.getHostname()) &&
          eq(((HostRequest) request).getHostAttributes(), hostRequest.getHostAttributes());
    }

    @Override
    public void appendTo(StringBuffer stringBuffer) {
      stringBuffer.append("HostRequestSetMatcher(").append(hostRequest).append(")");
    }
  }

  /**
   * Matcher for a ServiceComponentHostRequest set containing a single request.
   */
  public static class HostComponentRequestSetMatcher extends HashSet<ServiceComponentHostRequest>
      implements IArgumentMatcher {

    private final ServiceComponentHostRequest hostComponentRequest;

    public HostComponentRequestSetMatcher(String clusterName, String serviceName, String componentName, String hostName,
                                      Map<String, String> configVersions, String desiredState) {
      this.hostComponentRequest =
          new ServiceComponentHostRequest(clusterName, serviceName, componentName,
              hostName, desiredState);
      add(this.hostComponentRequest);
    }

    @Override
    public boolean matches(Object o) {

      if (!(o instanceof Set)) {
        return false;
      }

      Set set = (Set) o;

      if (set.size() != 1) {
        return false;
      }

      Object request = set.iterator().next();

      return request instanceof ServiceComponentHostRequest &&
          eq(((ServiceComponentHostRequest) request).getClusterName(), hostComponentRequest.getClusterName()) &&
          eq(((ServiceComponentHostRequest) request).getServiceName(), hostComponentRequest.getServiceName()) &&
          eq(((ServiceComponentHostRequest) request).getComponentName(), hostComponentRequest.getComponentName()) &&
          eq(((ServiceComponentHostRequest) request).getHostname(), hostComponentRequest.getHostname()) &&
          eq(((ServiceComponentHostRequest) request).getDesiredState(), hostComponentRequest.getDesiredState());
    }

    @Override
    public void appendTo(StringBuffer stringBuffer) {
      stringBuffer.append("HostComponentRequestSetMatcher(").append(hostComponentRequest).append(")");
    }
  }

  /**
   * Matcher for a TaskStatusRequest set containing a single request.
   */
  public static class TaskRequestSetMatcher extends HashSet<TaskStatusRequest> implements IArgumentMatcher {

    private final TaskStatusRequest taskStatusRequest;

    public TaskRequestSetMatcher(Long requestId, Long taskId) {
      this.taskStatusRequest = new TaskStatusRequest(requestId, taskId);
      add(this.taskStatusRequest);
    }

    @Override
    public boolean matches(Object o) {

      if (!(o instanceof Set)) {
        return false;
      }

      Set set = (Set) o;

      if (set.size() != 1) {
        return false;
      }

      Object request = set.iterator().next();

      return request instanceof TaskStatusRequest &&
          eq(((TaskStatusRequest) request).getRequestId(), taskStatusRequest.getRequestId());
    }

    @Override
    public void appendTo(StringBuffer stringBuffer) {
      stringBuffer.append("TaskRequestSetMatcher(").append(taskStatusRequest).append(")");
    }
  }

  /**
   * Matcher for a UserRequest set containing a single request.
   */
  public static class UserRequestSetMatcher extends HashSet<UserRequest> implements IArgumentMatcher {

    private final UserRequest userRequest;

    public UserRequestSetMatcher(String name) {
      this.userRequest = new UserRequest(name);
      add(this.userRequest);
    }

    @Override
    public boolean matches(Object o) {

      if (!(o instanceof Set)) {
        return false;
      }

      Set set = (Set) o;

      if (set.size() != 1) {
        return false;
      }

      Object request = set.iterator().next();

      return request instanceof UserRequest &&
          eq(((UserRequest) request).getUsername(), userRequest.getUsername());
    }

    @Override
    public void appendTo(StringBuffer stringBuffer) {
      stringBuffer.append("UserRequestSetMatcher(").append(userRequest).append(")");
    }
  }

  /**
   * Matcher for a GroupRequest set containing a single request.
   */
  public static class GroupRequestSetMatcher extends HashSet<GroupRequest> implements IArgumentMatcher {

    private final GroupRequest groupRequest;

    public GroupRequestSetMatcher(String name) {
      this.groupRequest = new GroupRequest(name);
      add(this.groupRequest);
    }

    @Override
    public boolean matches(Object o) {

      if (!(o instanceof Set)) {
        return false;
      }

      Set set = (Set) o;

      if (set.size() != 1) {
        return false;
      }

      Object request = set.iterator().next();

      return request instanceof GroupRequest &&
          eq(((GroupRequest) request).getGroupName(), groupRequest.getGroupName());
    }

    @Override
    public void appendTo(StringBuffer stringBuffer) {
      stringBuffer.append("GroupRequestSetMatcher(").append(groupRequest).append(")");
    }
  }

  /**
   * Matcher for a MemberRequest set containing a single request.
   */
  public static class MemberRequestSetMatcher extends HashSet<MemberRequest> implements IArgumentMatcher {

    private final MemberRequest memberRequest;

    public MemberRequestSetMatcher(String groupname, String username) {
      this.memberRequest = new MemberRequest(groupname, username);
      add(this.memberRequest);
    }

    @Override
    public boolean matches(Object o) {

      if (!(o instanceof Set)) {
        return false;
      }

      Set set = (Set) o;

      if (set.size() != 1) {
        return false;
      }

      Object request = set.iterator().next();

      return request instanceof MemberRequest &&
          eq(((MemberRequest) request).getGroupName(), memberRequest.getGroupName()) &&
          eq(((MemberRequest) request).getUserName(), memberRequest.getUserName());
    }

    @Override
    public void appendTo(StringBuffer stringBuffer) {
      stringBuffer.append("MemberRequestSetMatcher(").append(memberRequest).append(")");
    }
  }

  /**
   * Matcher for a Stack set containing a single request.
   */
  public static class StackConfigurationRequestSetMatcher extends HashSet<StackConfigurationRequest> implements IArgumentMatcher {

    private final StackConfigurationRequest stackConfigurationRequest;

    public StackConfigurationRequestSetMatcher(String stackName, String stackVersion,
        String serviceName, String propertyName) {
      this.stackConfigurationRequest = new StackConfigurationRequest(stackName, stackVersion, serviceName, propertyName);
      add(this.stackConfigurationRequest);
    }

    @Override
    public boolean matches(Object o) {

      if (!(o instanceof Set)) {
        return false;
      }

      Set set = (Set) o;

      if (set.size() != 1) {
        return false;
      }

      Object request = set.iterator().next();

      return request instanceof StackConfigurationRequest &&
          eq(((StackConfigurationRequest) request).getPropertyName(), stackConfigurationRequest.getPropertyName()) &&
          eq(((StackConfigurationRequest) request).getServiceName(), stackConfigurationRequest.getServiceName()) &&
          eq(((StackConfigurationRequest) request).getStackName(), stackConfigurationRequest.getStackName()) &&
          eq(((StackConfigurationRequest) request).getStackVersion(), stackConfigurationRequest.getStackVersion());
    }

    @Override
    public void appendTo(StringBuffer stringBuffer) {
      stringBuffer.append("StackConfigurationRequestSetMatcher(").append(stackConfigurationRequest).append(")");
    }
  }

  /**
   * A test observer that records the last event.
   */
  public static class TestObserver implements ResourceProviderObserver {

    ResourceProviderEvent lastEvent = null;

    @Override
    public void update(ResourceProviderEvent event) {
      lastEvent = event;
    }

    public ResourceProviderEvent getLastEvent() {
      return lastEvent;
    }
  }


  // ----- Test resource adapter ---------------------------------------------

  private static Resource.Type testResourceType = new Resource.Type("testResource");

  private static Set<String> pkPropertyIds =
      new HashSet<String>(Arrays.asList(new String[]{
          "ClusterName",
          "ResourceName"}));

  private static Set<String> propertyIds =
      new HashSet<String>(Arrays.asList(new String[]{
          "ClusterName",
          "ResourceName",
          "SomeProperty",
          "SomeOtherProperty"}));

  private static Map<Resource.Type, String> keyPropertyIds =
      new HashMap<Resource.Type, String>();

  static {
    keyPropertyIds.put(Resource.Type.Cluster, "ClusterName");
    keyPropertyIds.put(testResourceType, "ResourceName" );
  }

  private static Set<Resource> allResources = new HashSet<Resource>();

  static {
    Resource resource = new ResourceImpl(testResourceType);
    resource.setProperty("ClusterName", "c1");
    resource.setProperty("ResourceName", "r1");
    resource.setProperty("SomeProperty", "SomeValue1");
    resource.setProperty("SomeOtherProperty", 10);
    allResources.add(resource);

    resource = new ResourceImpl(testResourceType);
    resource.setProperty("ClusterName", "c1");
    resource.setProperty("ResourceName", "r2");
    resource.setProperty("SomeProperty", "SomeValue2");
    resource.setProperty("SomeOtherProperty", 100);
    allResources.add(resource);

    resource = new ResourceImpl(testResourceType);
    resource.setProperty("ClusterName", "c1");
    resource.setProperty("ResourceName", "r3");
    resource.setProperty("SomeProperty", "SomeValue3");
    resource.setProperty("SomeOtherProperty", 1000);
    allResources.add(resource);

    resource = new ResourceImpl(testResourceType);
    resource.setProperty("ClusterName", "c1");
    resource.setProperty("ResourceName", "r4");
    resource.setProperty("SomeProperty", "SomeValue4");
    resource.setProperty("SomeOtherProperty", 9999);
    allResources.add(resource);
  }

  public static class TestResourceProvider extends AbstractResourceProvider {

    protected TestResourceProvider() {
      super(propertyIds, keyPropertyIds);
    }

    @Override
    protected Set<String> getPKPropertyIds() {
      return pkPropertyIds;

    }

    @Override
    public RequestStatus createResources(Request request)
        throws SystemException, UnsupportedPropertyException, ResourceAlreadyExistsException, NoSuchParentResourceException {
      return new RequestStatusImpl(null);
    }

    @Override
    public Set<Resource> getResources(Request request, Predicate predicate)
        throws SystemException, UnsupportedPropertyException, NoSuchResourceException, NoSuchParentResourceException {

      Set<Resource> resources = new HashSet<Resource>();

      for(Resource resource : allResources) {
        if (predicate.evaluate(resource)) {
          resources.add(new ResourceImpl(resource, request.getPropertyIds()));
        }
      }
      return resources;
    }

    @Override
    public RequestStatus updateResources(Request request, Predicate predicate)
        throws SystemException, UnsupportedPropertyException, NoSuchResourceException, NoSuchParentResourceException {
      return new RequestStatusImpl(null);
    }

    @Override
    public RequestStatus deleteResources(Predicate predicate)
        throws SystemException, UnsupportedPropertyException, NoSuchResourceException, NoSuchParentResourceException {
      return new RequestStatusImpl(null);
    }
  }
}
