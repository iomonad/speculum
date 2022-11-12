<a href="https://github.com/iomonad/gtekportail"><img
  src=".github/mirror.png" 
  height="350" align="right"></a>
  
# Speculum [![Clojure CI](https://github.com/iomonad/speculum/actions/workflows/clojure.yml/badge.svg)](https://github.com/iomonad/speculum/actions/workflows/clojure.yml)
> Mirror XYZ Raster Tiles locally, rebuild the server arborescence and estimate total mirror coverage

## Rationale

The goal of Speculum is to provide kind of disk backed Tiles & WMS cache that work 
transparently like a proxy on your data provider. The cache is populated incrementally 
depending client requests and are served from disk in priority.

This enable you to partially mirror data from your provider, and reduce requests on it,
acting like a local proxy.

## Building

```bash
lein uberjar
```

## Changelog

### 0.1.8-SNAPSHOT

### 0.1.7
- Parametrized connection pool usage
- Removed hash support for the moment, improve request responses performances
  significantly
- Removed lag preview in ping routes, use total indexed tiles instead

### 0.1.6
- Filesystem tree is checked in priority over memory

### 0.1.5
- make preview route falcultative via component definition

### 0.1.4
- fix: webserver listen on all interfaces

### 0.1.2
- Component default values
- Granular configurations for `config` component

### 0.1.1

- WMS Mirroring Support
- Fix: Avoid return of warn tile on 404 - design choice

### 0.1.0

- Naive ressource hashing and XYZ raster tile mirroring support
- Basic preview screen with Leaflet to estimage missing parts

## Disclaimer

This project can be subjected to Copyright infringement. After using the application, 
be sure to don't violate creator’s exclusive rights to the work without permission.

Speculum is an experimental project, use at you own risk.

## License

Copyright © 2022 iomonad <iomonad@riseup.net>

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
