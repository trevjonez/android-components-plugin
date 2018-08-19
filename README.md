### Android-Components

Variant aware artifact publishing for android projects.

Currently this project is a POC of functionality with the new `ComponentWithVariants` and `.module` meta data format in combination with android library projects.

- [ ] Different plugin for each android artifact type
- [ ] Delegate to various plugins from main plugin
- [ ] Lazy publications api usage when 4.10 releases
- [ ] File bug with aosp about kotlin version issue in 3.3.0-a5
- [ ] plugin portal publishing setup?
- [ ] auto create sources tasks and artifacts
- [ ] auto create javadoc/kdoc tasks and artifacts
- [ ] detect proguard mapping and create artifacts for that
- [ ] write a real readme
- [ ] dsl to disable the auto publishing of artifacts

## License

    Copyright 2018 Trevor Jones

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.