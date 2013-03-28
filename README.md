XModel
======

Hierarchical, heterogenous, application data-modeling and data-binding library based on XPath querying.

This library was designed to allow a heterogenous collection of data sources, such as relational databases,
document databases, file-system, networked servers, etc..., to be united into a hierarchical application
data-model that can be queried and query-bound using the XPath query language.  Such a data-model will likely
exceed the size of available memory, so the framework is based around caching policies that how data is loaded
and purged from the memory.

A core feature of the library is XPath query binding.  Query binding allows clients to watch the data-model
through the lens of an XPath query.  The data-model can efficiently bind and unbind thousands of queries, 
while allowing unused data to be purged from memory under the constraints of the caching policies.

Although the XModel is useful in stand-alone, it is the basis of the Xidget multi-platform UI development
framework.
