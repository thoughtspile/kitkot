package sample.storage

class AutoIncrementStore<Item>(val name: String) {
    var items = listOf<Item>()
        private set

    @Synchronized
    fun insert(build: (id: Int) -> Item): Item {
        val item = build(items.size)
        items += item
        return item
    }

    fun update(id: Int, action: Item.() -> Unit) {
        items.getOrNull(id)?.action() ?: error("invalid $name ID")
    }
}