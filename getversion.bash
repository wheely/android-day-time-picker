#!/bin/bash
sed "/.*android-day-time-picker.*/!d ; s/.*android-day-time-picker:\([^']*\).*/\1/" README.md
