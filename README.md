<img width="419" height="888" alt="image" src="https://github.com/user-attachments/assets/20dabacf-2b71-4689-ae4e-4a98e5ef54f1" /># 🃏 Yu-Gi-Oh Fusion Calculator

**Version 1.5b** | An advanced Android app for calculating fusion combinations in Yu-Gi-Oh: Forbidden Memories

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![API](https://img.shields.io/badge/API-27%2B-brightgreen.svg)](https://android-arsenal.com/api?level=27)
[![Java](https://img.shields.io/badge/Language-Java-orange.svg)](https://java.com)

---

## 📱 Overview

The **Yu-Gi-Oh Fusion Calculator** is a comprehensive Android application designed to help players find optimal fusion combinations in Yu-Gi-Oh: Forbidden Memories. With an intuitive interface and powerful calculation engine, this app takes the guesswork out of fusion strategies.

### ✨ Key Highlights
- **721+ Cards** - Complete card database with detailed information
- **Advanced Fusion Engine** - Supports direct, chained, and field-based fusions  
- **Smart Interface** - Expandable hand system with drag-and-drop functionality
- **Drop Information** - Know exactly where to find each card
- **Memory System** - Learns from your usage patterns for better recommendations

---

## 🚀 Features

### 🎯 Core Functionality

#### **Advanced Fusion Calculator**
- **Direct Fusions** - Standard 2-card combinations (A + B = C)
- **Chained Fusions** - Complex 3-card combinations ((A + B = X) + C = Result)
- **Field Fusions** - Combinations using cards from both hand and field
- **Real-time Calculation** - Instant results as you add/remove cards

#### **Dynamic Hand & Field Management**
- **Expandable Hand System** - Start with 6 slots, expand up to 15 cards
- **Field Strategy** - 5 dedicated field slots for tactical placement
- **Auto-Repositioning** - Smart card organization (toggleable)
- **Quick Actions** - Move cards between hand and field with one tap

#### **Intelligent Sorting & Filtering**
- **Sort by Attack** - Find the strongest fusion results first
- **Sort by Card Count** - Prioritize easier fusions (fewer materials)
- **Smart Search** - Live search by card name or ID
- **Usage Priority** - Recently used cards appear first in searches

### 📚 Comprehensive Card Library

#### **Complete Database**
- **721+ Cards** - Every card from Yu-Gi-Oh: Forbidden Memories
- **Detailed Information** - Attack points, types, fusion recipes
- **High-Quality Images** - Visual card recognition
- **Grid View** - Browse cards with intuitive layout

#### **Drop Location Guide**
- **Opponent Information** - Know which duelist drops each card
- **Drop Rates** - Exact probabilities (out of 2048) and percentages
- **Difficulty Levels** - S/A, POW ratings for each opponent
- **Strategic Planning** - Plan your farming routes efficiently

### 🧠 Smart Features

#### **Card Memory System**
- **Usage Tracking** - Remembers your most-used cards
- **Smart Suggestions** - Prioritizes familiar cards in search results
- **Learning Algorithm** - Adapts to your play style over time

#### **Customizable Settings**
- **Auto-Repositioning** - Toggle automatic hand organization
- **Auto-Close Dialogs** - Streamline your workflow
- **Persistent Preferences** - Your settings are remembered

---

## 📱 Screenshots

> *Screenshots will be added here showing the main interface, fusion results, and card library*

```
<img width="419" height="888" alt="image" src="https://github.com/user-attachments/assets/67e30fe6-b636-461d-8dee-94389971a354" />
<img width="423" height="891" alt="image" src="https://github.com/user-attachments/assets/54f9c325-31a8-4da0-8c5a-c77905354a18" />
<img width="415" height="889" alt="image" src="https://github.com/user-attachments/assets/0bf3533a-5b16-41a1-81f0-03962e1fcabd" />
<img width="419" height="911" alt="image" src="https://github.com/user-attachments/assets/32cde6dc-8c48-48b0-9b54-12ea2b92947f" />

```

---

## ⚙️ Technical Specifications


### **Development Stack**
- **Language**: Java
- **SDK Version**: Target 34 (Android 14)
- **UI Framework**: Material Design Components
- **Data Storage**: JSON + SharedPreferences
- **Architecture**: MVP Pattern

### **Dependencies**
```gradle
• androidx.appcompat:appcompat:1.6.1
• com.google.android.material:material:1.11.0
• androidx.recyclerview:recyclerview:1.3.2
• androidx.cardview:cardview:1.0.0
• androidx.constraintlayout:constraintlayout:2.1.4
```

---

## 🛠️ Installation

### **Download & Install**
1. Download the APK from the releases section
2. Enable "Install from Unknown Sources" in Android settings
3. Install the APK file
4. Grant necessary permissions
5. Launch the app and start calculating!

### **Building from Source**
```bash
# Clone the repository
git clone https://github.com/yourusername/ygo-fusion-calculator.git

# Open in Android Studio
cd ygo-fusion-calculator

# Build the project
./gradlew assembleDebug

# Install on device
./gradlew installDebug
```

---

## 📖 Usage Guide

### **Getting Started**
1. **Add Cards to Hand** - Tap empty slots or use "Add Card" button
2. **Search Cards** - Use name or ID search with live results
3. **View Fusion Results** - Possible combinations appear automatically
4. **Perform Fusions** - Tap fusion results to execute on field
5. **Manage Field** - Strategic placement of fusion results

### **Pro Tips**
- Use **hand expansion** for complex strategies requiring more cards
- **Sort by card count** to find easier fusions first  
- **Enable auto-repositioning** to keep your hand organized
- **Check drop locations** to plan efficient card farming
- Use the **card library** to explore all available options

### **Interface Overview**
```
┌─────────────────────────────────┐
│           Toolbar               │
├─────────────────────────────────┤
│         Hand (6-15 slots)       │
├─────────────────────────────────┤
│       Fusion Results            │
│    [Sort Options] [Filters]     │
│       • Direct Fusions          │
│       • Chained Fusions         │
│       • Field Fusions           │
├─────────────────────────────────┤
│         Field (5 slots)         │
└─────────────────────────────────┘
```

---

## 📊 Data Sources

This app utilizes comprehensive Yu-Gi-Oh: Forbidden Memories data including:

- **Official Card Database** - 721+ cards with complete statistics
- **Fusion Recipe Database** - Thousands of fusion combinations  
- **Card Type Classifications** - Accurate type and attribute data
- **Drop Location Database** - Complete opponent and drop rate information (May be incorrect)
- **Equipment & Magic Cards** - Support cards and enhancement data

### **Data Format**
- **Card Data**: JSON format with ID, name, type, attack, image references
- **Fusion Data**: JSON arrays with material and result mappings
- **Drop Data**: Text-based opponent listings with probability calculations
- **Images**: Organized as c000.jpg through c720.jpg format
### **Data Format**
- **Card Data**: JSON format with ID, name, type, attack, image references
---

## 🤝 Credits & Acknowledgments

### **Special Thanks**
Thanks to **Kurogami2134** for providing valuable resources at:  
**https://github.com/Kurogami2134/ygofm_fusioncalc**

Your contribution helped make this app possible!

### **Community Recognition**
Special thanks to the Yu-Gi-Oh community for maintaining accurate card databases and fusion guides that made this calculator possible. This includes:
- Card database maintainers
- Fusion recipe researchers  
- Drop rate data collectors
- Game mechanic analysts

---

## 🛡️ License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

### **Disclaimer**
This is an unofficial fan-made application. Yu-Gi-Oh! is a trademark of Konami Digital Entertainment. This app is not affiliated with or endorsed by Konami.

---

## 🤝 Contributing

We welcome contributions from the community! Here's how you can help:

### **How to Contribute**
1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/amazing-feature`)
3. **Commit your changes** (`git commit -m 'Add amazing feature'`)
4. **Push to the branch** (`git push origin feature/amazing-feature`)
5. **Open a Pull Request**

### **Areas for Contribution**
- 🐛 **Bug fixes** and stability improvements
- ✨ **New features** and enhancements
- 🎨 **UI/UX improvements** and material design updates
- 📱 **Performance optimizations** and memory management
- 🌍 **Internationalization** and localization
- 📖 **Documentation** improvements

### **Development Guidelines**
- Follow Android development best practices
- Use Java coding standards
- Test on multiple Android versions
- Update documentation for new features

---

## 🐛 Bug Reports & Feature Requests

### **Found a Bug?**
Please create an issue with:
- Android version and device model
- Steps to reproduce the issue
- Expected vs actual behavior
- Screenshots (if applicable)

### **Have a Feature Idea?**
We'd love to hear your suggestions! Please include:
- Clear description of the feature
- Use case and benefits
- Any implementation ideas

---

## 📈 Version History

### **v1.5b (Current)**
- ✨ Added Information dialog with app details
- 🎯 Enhanced fusion calculation accuracy
- 🔧 Improved UI responsiveness
- 📱 Better Android 14 compatibility

### **Previous Versions**
*Version history will be updated as releases are tagged*

---

## 🙏 Support the Project

If this app has been helpful for your Yu-Gi-Oh dueling strategies, consider:
- ⭐ **Starring** this repository
- 🐛 **Reporting bugs** or suggesting features  
- 🤝 **Contributing** code or documentation
- 📢 **Sharing** with fellow duelists

---

<div align="center">

**Made with ❤️ for the Yu-Gi-Oh community**

[⬆ Back to Top](#-yu-gi-oh-fusion-calculator)

</div> 
