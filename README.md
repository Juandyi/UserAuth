# Workshop No. 2: System Design Document for HCT Survival Equity Prediction

[cite_start]This repository contains the detailed system architecture and design decisions for the predictive model focused on **Equity in Post-HCT Survival Predictions**, serving as the final deliverable for Workshop No. 2[cite: 9, 12].

---

## 1. Review Workshop #1 Findings

This section summarizes the critical outcomes from the initial systems analysis, which directly shaped the proposed architecture.

### How it was done (Process)

[cite_start]The initial analysis, detailed in the Workshop #1 PDF, was reviewed to extract the core constraints and system behaviors[cite: 15, 17].

### Main Findings

* [cite_start]**System Complexity and Non-linearity:** Post-HCT survival is a highly complex, non-linear system, making simple models insufficient to capture the patient's true trajectory[cite: 175, 179].
* [cite_start]**Critical Equity Constraint:** The primary metric, the **Stratified C-Index**, mandates that model performance must be fair and consistent across all ethnic subgroups[cite: 20, 198]. [cite_start]Equity is a non-negotiable architectural requirement[cite: 20].
* [cite_start]**Chaos and Sensitivity:** The system exhibits high sensitivity to variables like **patient age**, **disease risk indices**, and **genetic compatibility**[cite: 19, 184]. [cite_start]Small variations in these inputs can lead to significant changes in prognosis, highlighting the need for robust design strategies[cite: 187, 181].

---

## 2. Define System Requirements

The critical findings were translated into measurable design requirements, focusing on performance, reliability, and user-centric needs.

### How it was done (Process)

[cite_start]The identified system weaknesses (sensitivity, data disparities) were converted into measurable performance goals[cite: 17]. [cite_start]User-centric needs were also discussed[cite: 44].

### Key Requirements

1.  [cite_start]**Performance and Equity:** The solution must be evaluated not only for its overall accuracy but also for its equitable performance across different patient groups[cite: 44, 198]. [cite_start]The model performance must be consistent across ethnic subgroups[cite: 20, 198].
2.  [cite_start]**Reliability (Uncertainty):** The system must provide **Prediction Intervals** and **uncertainty bounds** to support clinical decision-making under uncertainty, particularly due to the system's sensitivity[cite: 158, 159, 160].
3.  [cite_start]**Interpretability:** The system must include a mechanism to generate detailed explanations for individual predictions (e.g., SHAP values) to support clinical decision-making and build trust across diverse populations[cite: 170].

---

## 3. High-Level Architecture

[cite_start]The system utilizes a **Modular Pipeline Architecture** to address complexity and integrate equity checks throughout the data flow[cite: 127].

### How it was done (Process)

[cite_start]A sequential data processing pipeline was designed, explicitly incorporating modules dedicated to the constraints identified in Workshop #1 (Equity, Uncertainty, etc.)[cite: 31, 127].

### Architectural Overview

[cite_start]The architecture is a sophisticated modular design composed of seven interconnected components[cite: 127, 202]:

1.  [cite_start]**Data Preprocessing:** Handles data cleaning, standardization, and uses **equity-aware imputation methods** to address missing data[cite: 130, 133].
2.  [cite_start]**Equity Analysis:** Specifically addresses fairness by performing **stratified analysis across demographic groups** and using **bias detection algorithms**[cite: 137, 139].
3.  [cite_start]**Feature Selection:** Selects robust, clinically relevant features, ensuring their equitable availability across all patient populations[cite: 142, 144].
4.  [cite_start]**Predictive Modeling Core:** The central prediction engine, employing an **Ensemble Approach** to combine Survival Analysis Models (Cox) with Machine Learning algorithms (GBMs)[cite: 146, 147, 148].
5.  [cite_start]**Fairness Calibration:** A post-processing step that adjusts model outputs to guarantee similar prediction accuracy across different patient populations, considering fairness metrics[cite: 152, 153].
6.  [cite_start]**Uncertainty Quantification:** Provides **prediction intervals** and **risk stratification with associated uncertainty bounds**[cite: 158, 159].
7.  [cite_start]**System Outputs:** Generates **Survival Probability Predictions**, an **Equity Metrics Dashboard**, and **Model Interpretability Outputs**[cite: 165, 168, 170].

### Systems Engineering Principles

* [cite_start]**Modularidad:** The architecture ensures the separation of core components to allow for independent testing, maintenance, and replacement[cite: 127].
* [cite_start]**Scalability:** The design supports the implementation of resource-intensive **Ensemble Methods** [cite: 206] [cite_start]and the processing of multi-faceted data[cite: 40].
* [cite_start]**Maintainability:** Clear component interfaces and system structure facilitate auditing and future updates of clinical rules or algorithms[cite: 127].

---

## 4. Addressing Sensitivity and Chaos

[cite_start]The design implements explicit measures to mitigate the unpredictable and highly sensitive behavior of the post-HCT system[cite: 18].

### How it was done (Process)

[cite_start]Design strategies were chosen based on Chaos Theory principles, focusing on dampening the "butterfly effect" (where small input changes lead to major output changes)[cite: 187, 188].

### Mitigation Strategies

* [cite_start]**Ensemble Modeling:** By combining multiple prediction models, the design is more robust against the randomness and noise inherent in biological systems[cite: 146, 206].
* [cite_start]**Uncertainty Quantification:** Providing confidence bounds formally manages the system's inherent unpredictability, advising clinicians when a prediction is less reliable[cite: 161, 158].
* [cite_start]**Feature Engineering:** Incorporating strong **Clinical Domain Knowledge** helps prevent the model from over-relying on unstable or noisy features[cite: 143, 23].

### Monitoring Routines

* [cite_start]**Concept Drift Monitoring:** The system requires continuous re-evaluation of the **Stratified C-Index** to detect performance degradation in specific subgroups, signalling that the underlying medical concept may have changed[cite: 198].
* [cite_start]**Data Drift Monitoring:** Should track changes in the distribution of high-sensitivity input variables (e.g., Age, Disease Risk Index) to detect shifts in the patient population[cite: 181, 184].

---

## 5. Technical Stack and Implementation Sketch

### How it was done (Process)

[cite_start]The technical stack was chosen to support the specific requirements of survival analysis, ensemble modeling, and fairness assessment[cite: 15, 147, 135].

### Recommended Technical Stack

* **Core Language:** **Python** (3.10+). This language is standard for machine learning and offers strong support for survival modeling and complex algorithms.
* [cite_start]**Modeling Libraries:** **`scikit-survival`** for Cox models and high-performance algorithms like **`XGBoost`** or **`LightGBM`** for the Ensemble Core[cite: 147, 148].
* [cite_start]**Fairness/Interpretability:** **`AIF360`** (Fairness) and **`SHAP`** (Interpretability) are necessary to implement the Equity Analysis (M2) and provide detailed explanations (M7)[cite: 139, 170].
* [cite_start]**Infrastructure:** **Docker** is essential for component containerization to ensure **reproducibility** and system stability[cite: 206].

### Implementation Plan and Design Patterns

1.  **Preprocessing & Equity (M1, M2):** Implemented using a **Data Transformer Pattern**. [cite_start]Each step (imputation, scaling, reweighting) is a distinct component, applying techniques like **fairness-aware preprocessing**[cite: 133, 140].
2.  [cite_start]**Modeling Core (M4):** Uses an **Ensemble Pattern (Stacking)** where the survival and machine learning models are combined to handle the system's complexity[cite: 146].
3.  **Calibration & Uncertainty (M5, M6):** These post-processing components are implemented using the **Decorator Pattern**. [cite_start]They wrap the raw output of the Modeling Core to apply necessary adjustments (fairness calibration and uncertainty calculation) without altering the core prediction logic[cite: 152, 158].

---

## 6. Documentation and Project Structure

The full analysis and design are available in the compiled report located in the `first_workshop` folder.

| Folder | Content |
| :--- | :--- |
| `first_workshop/` | **HCT Survival Equity System Analysis - LaTeX Document.pdf** (The complete Workshop #1 analysis) |
| `second_workshop/` | Source files for the current Workshop #2 (Design) |
| `README.md` | This document, explaining the development process |
