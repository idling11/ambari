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

package org.apache.ambari.server.controller.spi;


import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The resource object represents a requested resource.  The resource
 * contains a collection of values for the requested properties.
 */
public interface Resource {
  /**
   * Get the resource type.
   *
   * @return the resource type
   */
  public Type getType();

  /**
   * Obtain the properties contained by this group in a map structure.
   * The category/property hierarchy is flattened into a map where
   * each key is the absolute category name and the corresponding
   * value is a map of properties(name/value pairs) for that category.
   *
   * @return resource properties map
   */
  public Map<String, Map<String, Object>> getPropertiesMap();

  /**
   * Set a property value for the given property id on this resource.
   *
   * @param id    the property id
   * @param value the value
   */
  public void setProperty(String id, Object value);

  /**
   * Add an empty category to this resource.
   *
   * @param id the category id
   */
  public void addCategory(String id);

  /**
   * Get a property value for the given property id from this resource.
   *
   * @param id the property id
   * @return the property value
   */
  public Object getPropertyValue(String id);


  // ----- Enum : InternalType -----------------------------------------------

  /**
   * Enum of internal types.
   */
  public enum InternalType {
    Cluster,
    Service,
    Host,
    Component,
    HostComponent,
    Configuration,
    ConfigGroup,
    Action,
    Request,
    RequestSchedule,
    Task,
    User,
    Group,
    Member,
    Stack,
    StackVersion,
    OperatingSystem,
    Repository,
    StackService,
    StackConfiguration,
    StackServiceComponent,
    StackServiceComponentDependency,
    DRFeed,
    DRTargetCluster,
    DRInstance,
    Workflow,
    Job,
    TaskAttempt,
    RootService,
    RootServiceComponent,
    RootServiceHostComponent,
    View,
    ViewVersion,
    ViewInstance,
    Blueprint,
    HostComponentProcess,
    Permission;

    /**
     * Get the {@link Type} that corresponds to this InternalType.
     *
     * @return the corresponding type.
     */
    private Type getType() {
      String name = name();
      Type type = Type.getType(name);
      return type == null ? new Type(name(), ordinal()) : type;
    }
  }


  // ----- Inner class : Type ------------------------------------------------

  /**
   * Resource types.  Allows for the addition of external types.
   */
  public final class Type implements Comparable<Type>{

    /**
     * Map of all registered types.
     */
    private static Map<String, Type> types = new LinkedHashMap<String, Type>();

    /**
     * Ordinal number counter for registering external types.
     */
    private static int nextOrdinal = 10000;

    /**
     * Internal types.  See {@link InternalType}.
     */
    public static final Type Cluster = InternalType.Cluster.getType();
    public static final Type Service = InternalType.Service.getType();
    public static final Type Host = InternalType.Host.getType();
    public static final Type Component = InternalType.Component.getType();
    public static final Type HostComponent = InternalType.HostComponent.getType();
    public static final Type Configuration = InternalType.Configuration.getType();
    public static final Type ConfigGroup = InternalType.ConfigGroup.getType();
    public static final Type Action = InternalType.Action.getType();
    public static final Type Request = InternalType.Request.getType();
    public static final Type RequestSchedule = InternalType.RequestSchedule.getType();
    public static final Type Task = InternalType.Task.getType();
    public static final Type User = InternalType.User.getType();
    public static final Type Group = InternalType.Group.getType();
    public static final Type Member = InternalType.Member.getType();
    public static final Type Stack = InternalType.Stack.getType();
    public static final Type StackVersion = InternalType.StackVersion.getType();
    public static final Type OperatingSystem = InternalType.OperatingSystem.getType();
    public static final Type Repository = InternalType.Repository.getType();
    public static final Type StackService = InternalType.StackService.getType();
    public static final Type StackConfiguration = InternalType.StackConfiguration.getType();
    public static final Type StackServiceComponent = InternalType.StackServiceComponent.getType();
    public static final Type StackServiceComponentDependency = InternalType.StackServiceComponentDependency.getType();
    public static final Type DRFeed = InternalType.DRFeed.getType();
    public static final Type DRTargetCluster = InternalType.DRTargetCluster.getType();
    public static final Type DRInstance = InternalType.DRInstance.getType();
    public static final Type Workflow = InternalType.Workflow.getType();
    public static final Type Job = InternalType.Job.getType();
    public static final Type TaskAttempt = InternalType.TaskAttempt.getType();
    public static final Type RootService = InternalType.RootService.getType();
    public static final Type RootServiceComponent = InternalType.RootServiceComponent.getType();
    public static final Type RootServiceHostComponent = InternalType.RootServiceHostComponent.getType();
    public static final Type View = InternalType.View.getType();
    public static final Type ViewVersion = InternalType.ViewVersion.getType();
    public static final Type ViewInstance = InternalType.ViewInstance.getType();
    public static final Type Blueprint = InternalType.Blueprint.getType();
    public static final Type HostComponentProcess = InternalType.HostComponentProcess.getType();
    public static final Type Permission = InternalType.Permission.getType();

    /**
     * The type name.
     */
    private final String name;

    /**
     * The type ordinal value.
     */
    private final int ordinal;


    // ----- Constructors ----------------------------------------------------

    /**
     * Construct an internal type.
     *
     * @param name     the type name
     * @param ordinal  the ordinal number
     */
    private Type(String name, int ordinal) {
      assert(name != null);

      this.name    = name;
      this.ordinal = ordinal;

      setType(name, this);
    }

    /**
     * Construct an extended type.
     *
     * @param name  the type name
     */
    public Type(String name) {
      this(name, getNextOrdinal());
    }


    // ----- Resource.Type ---------------------------------------------------

    /**
     * Get the ordinal number for this type.
     *
     * @return the ordinal number
     */
    public final int ordinal() {
      return ordinal;
    }

    /**
     * Get the name for this type.
     *
     * @return the name
     */
    public String name() {
      return name;
    }

    /**
     * Indicate whether this is an internal type.
     *
     * @return true if this is an internal type; false otherwise
     */
    public boolean isInternalType() {
      return ordinal < InternalType.values().length;
    }

    /**
     * Get the internal type enum associated with this type.
     *
     * @return the internal type enum
     *
     * @throws UnsupportedOperationException if this type is not an internal type
     */
    public InternalType getInternalType() {
      if (isInternalType()) {
        return InternalType.values()[ordinal];
      }
      throw new UnsupportedOperationException(name + " is not an internal type.");
    }

    /**
     * Get the type associated with the given type name.
     *
     * @param name  the type name
     *
     * @return the associated type
     *
     * @throws IllegalArgumentException if the given type name is not associated with any known type
     */
    public static Type valueOf(String name) {
      Type type = types.get(name);
      if (type == null) {
        throw new IllegalArgumentException(name + " is not a type.");
      }
      return type;
    }

    /**
     * Get all of the known types.
     *
     * @return all of the types
     */
    public static Type[] values() {
      return types.values().toArray(new Type[types.size()]);
    }


    // ----- Object overrides ------------------------------------------------

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Type type = (Type) o;

      return ordinal == type.ordinal && name.equals(type.name);
    }

    @Override
    public int hashCode() {
      int result = name.hashCode();
      result = 31 * result + ordinal;
      return result;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
      throw new CloneNotSupportedException();
    }

    @Override
    public String toString() {
      return name;
    }


    // ----- Comparable<Type> ------------------------------------------------

    @Override
    public int compareTo(Type type) {
      return ordinal - type.ordinal();
    }


    // ----- helper methods --------------------------------------------------

    // get the next ordinal number
    private static synchronized int getNextOrdinal() {
      return nextOrdinal++;
    }

    // register the type by name
    private static synchronized void setType(String name, Type type) {
      types.put(name, type);
    }

    // get the type for the given name; null if not present
    private static Type getType(String name) {
      return types.get(name);
    }
  }
}
