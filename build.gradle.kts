object Product {
    const val version = "0.0.2-snapshot"
    const val group = "graymatter.sec"
}

allprojects {
    version = Product.version
    group = Product.group
}
