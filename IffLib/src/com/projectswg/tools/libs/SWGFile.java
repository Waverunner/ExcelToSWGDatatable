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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Waverunner on 6/4/2015
 */
public class SWGFile {
    private File file;
    private IffNode master;
    private IffNode currentForm;

    public SWGFile(String type) {
        this.master = new IffNode(type, true);
        this.currentForm = master;
    }

    public SWGFile(File file, String type) {
        this(type);
        this.file = file;
    }

    public void save(File file) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(file, false);
        outputStream.write(getData());
        outputStream.close();
    }

    public void save() throws IOException {
        save(file);
    }

    public void addForm(String tag) {
        addForm(tag, true);
    }

    public void addForm(String tag, boolean enterForm) {
        IffNode form = new IffNode(tag, true);
        currentForm.addChild(form);

        if (enterForm)
            currentForm = form;
    }

    public IffNode addChunk(String tag) {
        IffNode chunk = new IffNode(tag, false);
        currentForm.addChild(chunk);
        return chunk;
    }

    public byte[] getData() {
        return master.getBytes();
    }
}
