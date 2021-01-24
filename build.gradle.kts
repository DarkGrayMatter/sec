object Product {
    const val version = "0.0.1-SNAPSHOT"
    const val group = "org.becode.sec"
}


allprojects {
    version = Product.version
    group = Product.group
}
