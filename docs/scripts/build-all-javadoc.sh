#!/bin/bash

# Exit on error
set -e

echo "Generating Javadocs..."
mvn javadoc:aggregate

# Create target directory in docusaurus static folder
mkdir -p docs/static/javadoc

# Copy generated javadocs to docusaurus static folder
cp -r target/site/apidocs/* docs/static/javadoc/

echo "Javadocs generated and copied to docs/static/javadoc"
