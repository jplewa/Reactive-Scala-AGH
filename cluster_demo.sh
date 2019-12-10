#!/usr/bin/env bash

# first create ClusterWorkRouters cluster
sbt "runMain EShop.lab6.ProductCatalogClusterNodeApp seed-node1" &
sbt "runMain EShop.lab6.ProductCatalogClusterNodeApp seed-node2" &
sbt "runMain EShop.lab6.ProductCatalogClusterNodeApp seed-node3" &

# cluster at this point should be up and running

# starting http servers which will also create routers with workers deployed on previously configured cluster
sbt "runMain EShop.lab6.ProductCatalogHttpClusterApp 9003" &
sbt "runMain EShop.lab6.ProductCatalogHttpClusterApp 9004" &
sbt "runMain EShop.lab6.WorkHttpClusterApp 9005" &

# start gatling tests
#sbt gatling-it:test
#sbt gatling-it:lastReport
