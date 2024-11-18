# Gitlet: A Simplified Version-Control System

## Overview

Gitlet is a custom version-control system inspired by Git, developed to provide essential version-control functionality in a simplified and modular way. It allows users to track changes, manage branches, merge updates, and view project history—all through a command-line interface. This project demonstrates the implementation of core version-control concepts, efficient data structures, and robust algorithms in Java.

## Features

* Commit Tracking: Save the state of a directory and restore it at any point using unique identifiers.
* Branching and Merging: Create, switch, and merge branches, with conflict resolution using a graph-based commit history.
* Version History: View the chronological history of commits, including parents and branches.
* Staging Area: Track changes to files before committing, supporting both additions and removals.
* Conflict Detection and Resolution: Handle merge conflicts with an efficient algorithm for determining the lowest common ancestor (LCA).
* Command-Line Interface: User-friendly CLI for executing version-control commands (commit, checkout, branch, merge, etc.).


## Architecture

* Data Structures:
  * Hash Maps: Used for fast retrieval of commits, branches, and staged changes.
  * Graph-Based Commit History: Encodes commit relationships (parent-child) and supports branching and merging operations.
  * SHA-1 Hashing: Ensures unique identification of file snapshots and commits.
* Core Algorithms:
  * Recursive LCA Algorithm for efficient merge conflict detection.
  * Custom Diff Algorithm for comparing file changes.
* Modules:
  * Blob.java: Represents individual file snapshots.
  * Commit.java: Encapsulates commit metadata and content.
  * Branch.java: Manages branch-related operations.
  * Merge.java: Handles merging and conflict resolution.
  * Repo.java: Centralized repository manager for coordinating data and operations.
