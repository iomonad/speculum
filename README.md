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
