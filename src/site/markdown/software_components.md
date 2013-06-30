# Software components

[Component-based software engineering](http://en.wikipedia.org/wiki/Component-based_software_engineering)
defines a software component as "An individual software component is a software package, a web service,
a web resource, or a module that encapsulates a set of related functions (or data)."
While this description is true, it is far too generic to use directly in a project. The Nazgul project structure
therefore defines a software component in a more specific way.

# Nazgul project Software Components ("NSC")

The Nazgul project realizes software components as a set of collaborating Maven projects, each with a
separate role/purpose. The overall purpose of the software component is to reduce dependency
tanglements and to improve usability by design.

Each Nazgul software component may have two publicly available projects which act as the interface of the NSC - the
model project and the API project, as illustrated in the first row in the table below. The NSC is termed "domain
component" if both model and API projects exist, and a "library component" if only the API project exist for the NSC
in question. As illustrated in the figure below, the API project may hold a dependency to the model project - but
not vice versa.

<table>
    <tr>
        <th width="50%">Description</th>
        <th width="50%">Image</th>
    </tr>
    <tr>
        <td>NSC publicly available projects. Note that the API project may hold a dependency to
        the model project - but not vice versa.</td>
        <td><img src="images/plantuml/modularity_component.png" style="margin:10px; border:1px solid black;" /></td>
    </tr>
    <tr>
        <td>Client calling a method within an NSC API project. Note that the client only holds a dependency to the
        foo_api project (and receives a transient dependency to the foo_model). The client project may not depend on
        private/internal NSC Foo projects; instead the implementation projects should be injected into applications
        (both JEE application projects, such as WARs or EARs, and standalone JSE application JARs).
        </td>
        <td><img src="images/plantuml/modularity_components.png" style="margin:10px; border:1px solid black;" /></td>
    </tr>
</table>

Client projects using the code defined within an NSC should depend only on the model and API projects. As
illustrated by the figure in the second row of the table above, a client component invokes on the Foo
component by importing a dependency on its API project (i.e. *foo_api* project). Since the foo_api project has a
transitive dependency on the foo_model, all entities defined within the foo_model are also immediately availabile to
the client project without a separate dependency import.

## Maven projects of Nazgul project Software Components

The NSC realization defines all permissible maven project types. Being OSGi-compliant by design, the model,
API and SPI project types export all packages as public, whereas the implementation projects hide all their classes
(i.e. having completely private packages).

In addition to the traditional model/api/spi/impl project structure, an important project for developers is the
example project intended to show the uses of the NSC. Example projects function as copy/paste sources,
implying that developers will need only copy code from test cases within the example project into their projects to
use the NSC in question. Frequently, developers will only need to investigate the API project as a reference.
This structure and philosophy has proven to save considerable amounts of time when developers learn how an NSC
works.

<table>
    <tr>
        <th width="50%">Description</th>
        <th width="50%">Image</th>
    </tr>
    <tr>
        <td>Structure of an example Nazgul Software Component ("NSC"), including permitted dependencies between
        its projects</td>
        <td><img src="images/plantuml/modularity_mavenProjects.png" style="margin:10px; border:1px solid black;"
        /></td>
    </tr>
</table>