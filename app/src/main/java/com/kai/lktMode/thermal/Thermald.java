/*
 * Copyright (C) 2015-2016 Willi Ye <williye97@gmail.com>
 *
 * This file is part of Kernel Adiutor.
 *
 * Kernel Adiutor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Kernel Adiutor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Kernel Adiutor.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.kai.lktMode.thermal;

import android.content.Context;

import com.kai.lktMode.bean.Utils;

/**
 * Created by willi on 12.05.16.
 */
public class Thermald {

    private static final String THERMALD = "thermald";


    public static boolean isThermaldEnabled() {
        return Utils.isPropRunning(THERMALD);
    }

    public static boolean supported() {
        return Utils.hasProp(THERMALD);
    }


}
