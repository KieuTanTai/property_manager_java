# RabbitMQ login implementation (detached)

This code has been moved out of `identity` and is not included in the backend Maven reactor. The `identity` module no longer declares RabbitMQ dependencies, queues, async login endpoints, or RabbitMQ connection settings.

The sources are retained here under their original Java package paths for reference. They depend on identity application and presentation types, so they are not a standalone module and are not compiled by the current build.
