package org.bukkit.craftbukkit.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.craftbukkit.inventory.BukkitMetaItem.SerializableMeta;
import org.bukkit.inventory.meta.BookMeta;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap.Builder;

@DelegateDeserialization(SerializableMeta.class)
class BukkitMetaBook extends BukkitMetaItem implements BookMeta {
    static final ItemMetaKey BOOK_TITLE = new ItemMetaKey("title");
    static final ItemMetaKey BOOK_AUTHOR = new ItemMetaKey("author");
    static final ItemMetaKey BOOK_PAGES = new ItemMetaKey("pages");
    static final int MAX_PAGE_LENGTH = 256;
    static final int MAX_TITLE_LENGTH = 0xffff;

    private String title;
    private String author;
    private List<String> pages = new ArrayList<String>();

    BukkitMetaBook(BukkitMetaItem meta) {
        super(meta);

        if (!(meta instanceof BukkitMetaBook)) {
            return;
        }
        BukkitMetaBook bookMeta = (BukkitMetaBook) meta;
        this.title = bookMeta.title;
        this.author = bookMeta.author;
        pages.addAll(bookMeta.pages);
    }

    BukkitMetaBook(NBTTagCompound tag) {
        super(tag);

        if (tag.hasKey(BOOK_TITLE.NBT)) {
            this.title = tag.getString(BOOK_TITLE.NBT);
        }

        if (tag.hasKey(BOOK_AUTHOR.NBT)) {
            this.author = tag.getString(BOOK_AUTHOR.NBT);
        }

        if (tag.hasKey(BOOK_PAGES.NBT)) {
            NBTTagList pages = tag.getTagList(BOOK_PAGES.NBT);
            String[] pageArray = new String[pages.tagCount()];

            for (int i = 0; i < pages.tagCount(); i++) {
                String page = ((NBTTagString) pages.tagAt(i)).data;
                pageArray[i] = page;
            }

            addPage(pageArray);
        }
    }

    BukkitMetaBook(Map<String, Object> map) {
        super(map);

        setAuthor(SerializableMeta.getString(map, BOOK_AUTHOR.BUKKIT, true));

        setTitle(SerializableMeta.getString(map, BOOK_TITLE.BUKKIT, true));

        Collection<?> pages = SerializableMeta.getObject(Collection.class, map, BOOK_PAGES.BUKKIT, true);
        BukkitMetaItem.safelyAdd(pages, this.pages, MAX_PAGE_LENGTH);
    }

    @Override
    void applyToItem(NBTTagCompound itemData) {
        super.applyToItem(itemData);

        if (hasTitle()) {
            itemData.setString(BOOK_TITLE.NBT, this.title);
        }

        if (hasAuthor()) {
            itemData.setString(BOOK_AUTHOR.NBT, this.author);
        }

        if (hasPages()) {
            itemData.setTag(BOOK_PAGES.NBT, createStringList(pages, BOOK_PAGES));
        }
    }

    @Override
    boolean isEmpty() {
        return super.isEmpty() && isBookEmpty();
    }

    boolean isBookEmpty() {
        return !(hasPages() || hasAuthor() || hasTitle());
    }

    @Override
    boolean applicableTo(Material type) {
        switch (type) {
        case WRITTEN_BOOK:
        case BOOK_AND_QUILL:
            return true;
        default:
            return false;
        }
    }

    public boolean hasAuthor() {
        return !Strings.isNullOrEmpty(author);
    }

    public boolean hasTitle() {
        return !Strings.isNullOrEmpty(title);
    }

    public boolean hasPages() {
        return !pages.isEmpty();
    }

    public String getTitle() {
        return this.title;
    }

    public boolean setTitle(final String title) {
        if (title == null) {
            this.title = null;
            return true;
        } else if (title.length() > MAX_TITLE_LENGTH) {
            return false;
        }

        this.title = title;
        return true;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }

    public String getPage(final int page) {
        Validate.isTrue(isValidPage(page), "Invalid page number");
        return pages.get(page - 1);
    }

    public void setPage(final int page, final String text) {
        if (!isValidPage(page)) {
            throw new IllegalArgumentException("Invalid page number " + page + "/" + pages.size());
        }

        pages.set(page - 1, text == null ? "" : text.length() > MAX_PAGE_LENGTH ? text.substring(0, MAX_PAGE_LENGTH) : text);
    }

    public void setPages(final String... pages) {
        this.pages.clear();

        addPage(pages);
    }

    public void addPage(final String... pages) {
        for (String page : pages) {
            if (page == null) {
                page = "";
            } else if (page.length() > MAX_PAGE_LENGTH) {
                page = page.substring(0, MAX_PAGE_LENGTH);
            }

            this.pages.add(page);
        }
    }

    public int getPageCount() {
        return pages.size();
    }

    public List<String> getPages() {
        return ImmutableList.copyOf(pages);
    }

    public void setPages(List<String> pages) {
        this.pages.clear();
        BukkitMetaItem.safelyAdd(pages, this.pages, MAX_PAGE_LENGTH);
    }

    private boolean isValidPage(int page) {
        return page > 0 && page <= pages.size();
    }

    @Override
    public BukkitMetaBook clone() {
        BukkitMetaBook meta = (BukkitMetaBook) super.clone();
        meta.pages = new ArrayList<String>(pages);
        return meta;
    }

    @Override
    int applyHash() {
        final int original;
        int hash = original = super.applyHash();
        if (hasTitle()) {
            hash = 61 * hash + this.title.hashCode();
        }
        if (hasAuthor()) {
            hash = 61 * hash + 13 * this.author.hashCode();
        }
        if (hasPages()) {
            hash = 61 * hash + 17 * this.pages.hashCode();
        }
        return original != hash ? BukkitMetaBook.class.hashCode() ^ hash : hash;
    }

    @Override
    boolean equalsCommon(BukkitMetaItem meta) {
        if (!super.equalsCommon(meta)) {
            return false;
        }
        if (meta instanceof BukkitMetaBook) {
            BukkitMetaBook that = (BukkitMetaBook) meta;

            return (hasTitle() ? that.hasTitle() && this.title.equals(that.title) : !that.hasTitle())
                    && (hasAuthor() ? that.hasAuthor() && this.author.equals(that.author) : !that.hasAuthor())
                    && (hasPages() ? that.hasPages() && this.pages.equals(that.pages) : !that.hasPages());
        }
        return true;
    }

    @Override
    boolean notUncommon(BukkitMetaItem meta) {
        return super.notUncommon(meta) && (meta instanceof BukkitMetaBook || isBookEmpty());
    }

    @Override
    Builder<String, Object> serialize(Builder<String, Object> builder) {
        super.serialize(builder);

        if (hasTitle()) {
            builder.put(BOOK_TITLE.BUKKIT, title);
        }

        if (hasAuthor()) {
            builder.put(BOOK_AUTHOR.BUKKIT, author);
        }

        if (hasPages()) {
            builder.put(BOOK_PAGES.BUKKIT, pages);
        }

        return builder;
    }

    @Override
    SerializableMeta.Deserializers deserializer() {
        return SerializableMeta.Deserializers.BOOK;
    }
}