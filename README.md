### Android-Components

Variant aware artifact publishing for android projects.

Currently this project is a POC of functionality with the new `ComponentWithVariants` and `.module` meta data format in combination with android library projects.

- [ ] License headers
- [ ] Split up the monolithic kt file
- [ ] Different plugin for each android artifact type
- [ ] Delegate to various plugins from main plugin
- [ ] Lazy publications api usage when 4.10 releases
- [ ] File bug with aosp about kotlin version issue in 3.3.0-a5
- [ ] plugin portal publishing setup?
- [ ] auto create sources tasks and artifacts
- [ ] auto create javadoc/kdoc tasks and artifacts
- [ ] detect proguard mapping and create artifacts for that
- [ ] write a real readme
- [ ] test multi dimensional app/lib permutations
- [ ] dsl to disable the auto publishing of artifacts