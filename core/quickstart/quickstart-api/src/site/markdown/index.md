# About the Nazgul Core: Quickstart API

The Quickstart Analyzer API defines services and methods that validate project structures, to ensure
conformity to a set of naming rules. More precisely, the quickstart API enables you to simply perform
validation and creation:
 
1. **Project Validation**. Validate that a project (including all its Software Component Maven projects) 
   conforms to a given naming standard. The standard itself is not provided by this quickstart API, but 
   rather within a particular implementation.
2. **Project Creation**. Create a new project including all its scaffolding directories, poms and parent/child
   relationships.
3. **Software Component Creation**. Simply create a new software component (a set of collaborating Maven Projects)
   with all inter-dependencies and appropriate patterns.

## Where can the Quickstart component/API be used?

The Quickstart API is particularly useful within quickstart tools such as 
[JBoss Forge](http://forge.jboss.org/) or 
[Maven Archetypes](http://maven.apache.org/guides/introduction/introduction-to-archetypes.html).
Again, the Analyzer API does not itself provide any actual naming or structure rules; these should instead
be located in an implementation project. This separation-of-concerns design implies that the given API can 
be used to validate and create project/structures for several different naming and structure standards.
 