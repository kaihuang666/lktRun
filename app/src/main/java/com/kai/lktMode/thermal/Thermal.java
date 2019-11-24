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

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by willi on 12.05.16.
 */
public class Thermal {
    List<ThermalBean> beans=new ArrayList<>();
    public static boolean supported() {
        return Thermald.supported() || MSMThermal.getInstance().supported();
    }

    public List<ThermalBean> getBeans() {
        return beans;
    }

    public Thermal(){
        MSMThermal thermal=MSMThermal.getInstance();
        if (thermal.hasCoreControl()){
            beans.add(new ThermalBean("核心控制", thermal.isCoreControlEnabled(), new ThermalBean.EnableClick() {
                @Override
                public void click(Boolean b) {
                    thermal.enableCoreControl(b);
                }
            }));
        }
        if (thermal.hasFreqLimitDebug()){
            beans.add(new ThermalBean("频率限制", thermal.isFreqLimitDebugEnabled(), new ThermalBean.EnableClick() {
                @Override
                public void click(Boolean b) {
                    thermal.enableFreqLimitDebug(b);
                }
            }));
        }
        if (thermal.hasImmediatelyLimitStop()){
            beans.add(new ThermalBean("即时核心限制", thermal.isImmediatelyLimitStopEnabled(), new ThermalBean.EnableClick() {
                @Override
                public void click(Boolean b) {
                    thermal.enableImmediatelyLimitStop(b);
                }
            }));
        }
        if (thermal.hasIntelliThermalEnable()){
            beans.add(new ThermalBean("智能温控", thermal.isIntelliThermalEnabled(), new ThermalBean.EnableClick() {
                @Override
                public void click(Boolean b) {
                    thermal.enableIntelliThermal(b);
                }
            }));
        }
        if (thermal.hasIntelliThermalOptimizedEnable()){
            beans.add(new ThermalBean("最优化温控", thermal.isIntelliThermalOptimizedEnabled(), new ThermalBean.EnableClick() {
                @Override
                public void click(Boolean b) {
                    thermal.enableIntelliThermalOptimized(b);
                }
            }));
        }
        if (thermal.hasTempSafety()){
            beans.add(new ThermalBean("温度保护", thermal.isTempSafetyEnabled(), new ThermalBean.EnableClick() {
                @Override
                public void click(Boolean b) {
                    thermal.enableTempSafety(b);
                }
            }));
        }
        if (thermal.hasTempThrottleEnable()){
            beans.add(new ThermalBean("温度门限", thermal.isTempThrottleEnabled(), new ThermalBean.EnableClick() {
                @Override
                public void click(Boolean b) {
                    thermal.enableTempThrottle(b);
                }
            }));
        }
        if (thermal.hasVddRestrictionEnable()){
            beans.add(new ThermalBean("VDD限制", thermal.isVddRestrictionEnabled(), new ThermalBean.EnableClick() {
                @Override
                public void click(Boolean b) {
                    thermal.enableVddRestriction(b);
                }
            }));
        }
        if (Thermald.supported()){
            beans.add(new ThermalBean("通用温控", Thermald.isThermaldEnabled(), new ThermalBean.EnableClick() {
                @Override
                public void click(Boolean b) {

                }
            }));
        }

    }
}
