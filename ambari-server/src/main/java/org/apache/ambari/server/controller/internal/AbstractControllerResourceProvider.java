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

import org.apache.ambari.server.controller.AmbariManagementController;
import org.apache.ambari.server.controller.ResourceProviderFactory;
import org.apache.ambari.server.controller.predicate.ArrayPredicate;
import org.apache.ambari.server.controller.predicate.EqualsPredicate;
import org.apache.ambari.server.controller.spi.Predicate;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.spi.ResourceProvider;
import org.apache.ambari.server.controller.utilities.ClusterControllerHelper;

import java.util.Map;
import java.util.Set;

/**
 * Abstract resource provider implementation that maps to an Ambari management controller.
 */
public abstract class AbstractControllerResourceProvider extends AbstractResourceProvider {

  private static ResourceProviderFactory resourceProviderFactory;
  /**
   * The management controller to delegate to.
   */
  private final AmbariManagementController managementController;


  // ----- Constructors ------------------------------------------------------

  /**
   * Create a  new resource provider for the given management controller.
   *
   * @param propertyIds          the property ids
   * @param keyPropertyIds       the key property ids
   * @param managementController the management controller
   */
  protected AbstractControllerResourceProvider(Set<String> propertyIds,
                                               Map<Resource.Type, String> keyPropertyIds,
                                               AmbariManagementController managementController) {
    super(propertyIds, keyPropertyIds);
    this.managementController = managementController;
  }

  public static void init(ResourceProviderFactory factory) {
    resourceProviderFactory = factory;
  }


  // ----- accessors ---------------------------------------------------------

  /**
   * Get the associated management controller.
   *
   * @return the associated management controller
   */
  protected AmbariManagementController getManagementController() {
    return managementController;
  }


  // ----- utility methods ---------------------------------------------------

  /**
   * Factory method for obtaining a resource provider based on a given type and management controller.
   *
   * @param type                  the resource type
   * @param propertyIds           the property ids
   * @param managementController  the management controller
   *
   * @return a new resource provider
   */
  public static ResourceProvider getResourceProvider(Resource.Type type,
                                                     Set<String> propertyIds,
                                                     Map<Resource.Type, String> keyPropertyIds,
                                                     AmbariManagementController managementController) {

    switch (type.getInternalType()) {
      case Cluster:
        return new ClusterResourceProvider(propertyIds, keyPropertyIds, managementController);
      case Service:
        return resourceProviderFactory.getServiceResourceProvider(propertyIds, keyPropertyIds, managementController);
      case Component:
        return resourceProviderFactory.getComponentResourceProvider(propertyIds, keyPropertyIds, managementController);
      case Host:
        return resourceProviderFactory.getHostResourceProvider(propertyIds, keyPropertyIds, managementController);
      case HostComponent:
        return resourceProviderFactory.getHostComponentResourceProvider(propertyIds, keyPropertyIds, managementController);
      case Configuration:
        return new ConfigurationResourceProvider(propertyIds, keyPropertyIds, managementController);
      case Action:
        return new ActionResourceProvider(propertyIds, keyPropertyIds, managementController);
      case Request:
        return new RequestResourceProvider(propertyIds, keyPropertyIds, managementController);
      case Task:
        return new TaskResourceProvider(propertyIds, keyPropertyIds, managementController);
      case User:
        return new UserResourceProvider(propertyIds, keyPropertyIds, managementController);
      case Group:
        return new GroupResourceProvider(propertyIds, keyPropertyIds, managementController);
      case Member:
        return resourceProviderFactory.getMemberResourceProvider(propertyIds, keyPropertyIds, managementController);
      case Stack:
        return new StackResourceProvider(propertyIds, keyPropertyIds, managementController);
      case StackVersion:
        return new StackVersionResourceProvider(propertyIds, keyPropertyIds, managementController);
      case StackService:
        return new StackServiceResourceProvider(propertyIds, keyPropertyIds, managementController);
      case StackServiceComponent:
        return new StackServiceComponentResourceProvider(propertyIds, keyPropertyIds, managementController);
      case StackConfiguration:
        return new StackConfigurationResourceProvider(propertyIds, keyPropertyIds, managementController);
      case OperatingSystem:
        return new OperatingSystemResourceProvider(propertyIds, keyPropertyIds, managementController);
      case Repository:
        return new RepositoryResourceProvider(propertyIds, keyPropertyIds, managementController);
      case RootService:
        return new RootServiceResourceProvider(propertyIds, keyPropertyIds, managementController);
      case RootServiceComponent:
        return new RootServiceComponentResourceProvider(propertyIds, keyPropertyIds, managementController);
      case RootServiceHostComponent:
        return new RootServiceHostComponentResourceProvider(propertyIds, keyPropertyIds, managementController);
      case ConfigGroup:
        return new ConfigGroupResourceProvider(propertyIds, keyPropertyIds, managementController);
      case RequestSchedule:
        return new RequestScheduleResourceProvider(propertyIds, keyPropertyIds, managementController);
      case HostComponentProcess:
        return new HostComponentProcessResourceProvider(propertyIds, keyPropertyIds, managementController);
      case Blueprint:
        return new BlueprintResourceProvider(propertyIds, keyPropertyIds, managementController);
      default:
        throw new IllegalArgumentException("Unknown type " + type);
    }
  }

  /**
   * Obtain a resource provider based on type.
   *
   * @param type  resource provider type
   *
   * @return resource provider for the specified type
   */
  ResourceProvider getResourceProvider(Resource.Type type) {
    return ((ClusterControllerImpl) ClusterControllerHelper.getClusterController()).
        ensureResourceProvider(type);
  }

  /**
   * Extracting given query_parameter value from the predicate
   * @param queryParameterId  query parameter id
   * @param predicate         predicate
   * @return the query parameter
   */
  protected static Object getQueryParameterValue(String queryParameterId, Predicate predicate) {

    Object result = null;

    if (predicate instanceof ArrayPredicate) {
      ArrayPredicate arrayPredicate  = (ArrayPredicate) predicate;
      for (Predicate predicateItem : arrayPredicate.getPredicates()) {
          if (predicateItem instanceof EqualsPredicate) {
            EqualsPredicate equalsPredicate =
                (EqualsPredicate) predicateItem;
            if (queryParameterId.equals(equalsPredicate.getPropertyId())) {
              return equalsPredicate.getValue();
            }
          } else {
            result = getQueryParameterValue(queryParameterId, predicateItem);
            if (result != null) {
              return result;
          }
        }
      }

    }
    return result;
  }
}
