# What is the Nazgul Framework: Core?

<img src="images/nazgul.jpg" style="float:right" width="203" height="236"/> The Nazgul Framework project holds a
collection of best-pracises and sensible configurations enabling you to start projects quickly and scale them to huge
sizes without having to change the development or deployment model. Moreover, the Nazgul Framework strives to increase
usability for the developers and architects working on a project, as well as reduce complexity/tanglement and increase
productivity.

The Nazgul Framework has two main components:

1. **Nazgul Framework: Tools**. The Nazgul Tools project (another reactor) aims to use best-of-breed tools to achieve a
    usable, well-composed and simple mode of development and deployment. It defines codestyle,
    IDE integration and global aspects usable in any project development.

2. **Nazgul Framework: Core**. The Nazgul Core project (this reactor) provides a set of commonly useable library
    tools, built on top of the Nazgul Tools codestyle, IDE integration and global aspects. These tools are mainly
    self-sustaining and are ready to be integrated and used in any project.

## How do I get started in using the Nazgul Core components?

The general approach should be:

1.  Create a dependency to the component's API project in your project. This is the dependency you should work
    with in the role of a calling client.

2.  If an example project exists, simply open its testcases. They should contain copy-and-paste code for
    Nazgul Software Component ("NSC") usage - you should be able to use it directly with your API dependency.

2.  If no example project exists, open the API project itself. Take a look at the interfaces and classes within
    the API, and optionally the unit tests.