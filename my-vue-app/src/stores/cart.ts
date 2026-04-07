import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface CartItem {
    menuId: number
    name: string
    price: number
    quantity: number
    imageUrl?: string
    note?: string
}

export const useCartStore = defineStore('cart', () => {
    const items = ref<CartItem[]>([])

    const totalCount = computed(() =>
        items.value.reduce((sum, item) => sum + item.quantity, 0)
    )

    const totalPrice = computed(() =>
        items.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
    )

    const addItem = (menu: { id: number; name: string; price: number; imageUrl?: string }, quantity = 1, note = '') => {
        const existing = items.value.find(i => i.menuId === menu.id)
        if (existing) {
            existing.quantity += quantity
        } else {
            items.value.push({
                menuId: menu.id,
                name: menu.name,
                price: menu.price,
                quantity,
                imageUrl: menu.imageUrl,
                note,
            })
        }
        saveToLocal()
    }

    const updateQuantity = (menuId: number, quantity: number) => {
        const item = items.value.find(i => i.menuId === menuId)
        if (item) {
            if (quantity <= 0) {
                removeItem(menuId)
            } else {
                item.quantity = quantity
                saveToLocal()
            }
        }
    }

    const removeItem = (menuId: number) => {
        items.value = items.value.filter(i => i.menuId !== menuId)
        saveToLocal()
    }

    const clearCart = () => {
        items.value = []
        saveToLocal()
    }

    const saveToLocal = () => {
        localStorage.setItem('cart', JSON.stringify(items.value))
    }

    const loadFromLocal = () => {
        const saved = localStorage.getItem('cart')
        if (saved) {
            items.value = JSON.parse(saved)
        }
    }

    return {
        items,
        totalCount,
        totalPrice,
        addItem,
        updateQuantity,
        removeItem,
        clearCart,
        loadFromLocal,
    }
})