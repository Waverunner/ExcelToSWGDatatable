/*******************************************************************************
 * Excel to SWG Iff Datatable
 * Copyright (C) 2015  Waverunner
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 ******************************************************************************/

package com.projectswg.tools.libs;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Waverunner on 6/7/2015
 */
public class IffNode {
    private String tag;
    private boolean isForm;
    private List<IffNode> children;
    private byte[] chunkData;

    public IffNode(String tag, boolean isForm) {
        this.tag = tag;
        this.isForm = isForm;
    }

    public void addChild(IffNode child) {
        if (children == null)
            children = new ArrayList<>();
        children.add(child);
    }

    public void setChunkData(ByteBuffer chunkData) {
        this.chunkData = chunkData.array();
    }

    public byte[] getBytes() {
        return isForm ? createForm() : createChunk();
    }

    private byte[] createForm() {
        List<byte[]> childrenData = new ArrayList<>();
        int size = 0;
        for (IffNode child : children) {
            byte[] subData = child.getBytes();
            size += subData.length;
            childrenData.add(subData);
        }
        ByteBuffer bb = ByteBuffer.allocate(size + 12).order(ByteOrder.LITTLE_ENDIAN);
        bb.put("FORM".getBytes(Charset.forName("US-ASCII")));
        bb.order(ByteOrder.BIG_ENDIAN).putInt(size + 4);
        bb.put(tag.getBytes(Charset.forName("US-ASCII")));
        childrenData.forEach(bytes -> bb.put(bytes));
        return bb.array();
    }

    private byte[] createChunk() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8 + chunkData.length).order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(tag.getBytes(Charset.forName("US-ASCII")));
        byteBuffer.order(ByteOrder.BIG_ENDIAN).putInt(chunkData.length);
        byteBuffer.put(chunkData);
        return byteBuffer.array();
    }
}
