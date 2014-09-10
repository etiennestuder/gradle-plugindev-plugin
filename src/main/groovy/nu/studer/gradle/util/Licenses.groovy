/*
 * Copyright 2014 Etienne Studer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nu.studer.gradle.util;

/**
 * Utility class for dealing with licenses.
 */
final class Licenses {

    public static final def LICENSE_TYPES = [
            'Apache-2.0': ['Apache License, Version 2.0', 'http://www.apache.org/licenses/LICENSE-2.0.html'],
            'GPL-3.0'   : ['GNU General Public License, Version 3.0', 'http://www.gnu.org/licenses/gpl.html'],
            'GPL-2.0'   : ['GNU General Public License, Version 2.0', 'http://www.gnu.org/licenses/old-licenses/gpl-2.0.html'],
            'GPL-1.0'   : ['GNU General Public License, Version 1.0', 'http://www.gnu.org/licenses/old-licenses/gpl-1.0.html'],
            'LGPL-3.0'  : ['GNU Lesser General Public License, Version 3.0', 'http://www.gnu.org/licenses/lgpl.html'],
            'LGPL-2.1'  : ['GNU Lesser General Public License, Version 2.1', 'http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html'],
            'MIT'       : ['MIT License', 'http://opensource.org/licenses/MIT']
    ]

    private Licenses() {
    }

}
