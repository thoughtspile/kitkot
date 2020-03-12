package sample.storage

class AutoIncrementStore<Item>(private val name: String) {
    var items = mutableListOf<Item>()
        private set

    @Synchronized
    fun insert(build: (id: Int) -> Item): Item {
        val item = build(items.size)
        items.add(item)
        return item
    }

    fun update(id: Int, action: Item.() -> Item) {
        items[id] = items.getOrNull(id)?.action() ?: error("invalid $name ID")
    }
}