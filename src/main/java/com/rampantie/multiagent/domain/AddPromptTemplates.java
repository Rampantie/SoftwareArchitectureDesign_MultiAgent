package com.rampantie.multiagent.domain;

public class AddPromptTemplates {

    public static final String HPS_BUSINESS_CONTEXT = """
            ## Hotel Pricing System (HPS) Business Context

            ### Main Use Cases
            - **HPS-1: Login** - User provides credentials in login window, system verifies against identity service, upon success grants access only to authorized hotels
            - **HPS-2: Change Price** - User selects hotel and date, can simulate or submit base room rate changes, all derived prices calculated immediately and pushed to channel management system
            - **HPS-3: Query Price** - User or external system queries price for given hotel through UI or API
            - **HPS-4: Manage Hotel** - Administrator adds/modifies hotel information (tax rates, available room types, etc.)
            - **HPS-5: Manage Room Rate** - Administrator adds/modifies room rates and defines business rules for different rate types
            - **HPS-6: Manage Users** - Administrator modifies user permissions

            ### Key Quality Attributes & Scenarios
            - **Q-1 Performance**: After changing base room rate, all prices published within <100ms [High importance, High difficulty]
            - **Q-2 Reliability**: 100% of price changes successfully published and received by channel system [High importance, High difficulty]
            - **Q-3 Availability**: Price query SLA 99.9% uptime (excluding maintenance windows) [High importance, High difficulty]
            - **Q-4 Scalability**: Support 100,000 to 1,000,000 queries/day with average latency increase ≤20% [High importance, High difficulty]
            - **Q-5 Security**: Login verification, permission control, credential secure storage [High importance, Medium difficulty]
            - **Q-6 Modifiability**: Adding gRPC query endpoint requires no changes to core components [Medium importance, Medium difficulty]
            - **Q-7 Deployability**: Application migration across non-production environments requires no code changes [Medium importance, Medium difficulty]
            - **Q-8 Monitorability**: Mechanism to collect 100% of price publication performance/reliability measurement data [Medium importance, Medium difficulty]
            - **Q-9 Testability**: 100% of system supports integration testing independent of external systems [Medium importance, Medium difficulty]

            ### Architecture Concerns
            - **CRN-1**: Establish overall initial system structure
            - **CRN-2**: Leverage team expertise in Java, Angular, Kafka
            - **CRN-3**: Allocate work to development team members
            - **CRN-4**: Avoid introducing technical debt
            - **CRN-5**: Establish continuous deployment infrastructure

            ### Constraints
            - **CON-1**: Users interact through web browser, support cross-platform (Windows/OSX/Linux/multiple devices)
            - **CON-2**: Identity service via cloud provider, resources hosted in cloud
            - **CON-3**: Code hosted on proprietary Git platform
            - **CON-4**: Full version delivery within 6 months, MVP demo to stakeholders within 2 months
            - **CON-5**: Initially use REST API, may support other protocols later
            - **CON-6**: Prefer cloud-native approach
            """;

    public static final String ADD_3_0_FRAMEWORK = """
            ## ADD 3.0 Architecture Design Methodology

            ADD (Attribute-Driven Design) iteratively designs system architecture through 7 steps:

            **Step 1 - Review Inputs**: Identify architecture driving factors (requirements, quality attributes, constraints, concerns), determine design priorities
            **Step 2 - Determine Iteration Objective**: Select key driving factors to address in this iteration
            **Step 3 - Choose System Elements**: Select architecture elements to refine/design (system, subsystem, modules)
            **Step 4 - Choose Design Concept**: Evaluate multiple feasible design options, select best option
            **Step 5 - Instantiate Architecture Elements**: Based on selected option, define concrete components, responsibilities, interfaces, relationships
            **Step 6 - Sketch Views, Record Decisions**: Generate architecture views (C1-system context, C2-container, C3-component, etc.), record key decisions and rationale
            **Step 7 - Analyze Design**: Check if iteration objectives and driving factors are met, determine if further iterations needed

            ### Output Requirements
            Strictly follow this format, use separators to mark results of each step:

            --- STEP 1 OUTPUT ---
            [Results of review inputs: identified driving factors, priority ordering]

            --- STEP 2 OUTPUT ---
            [Design objective and focus of this iteration]

            --- STEP 3 OUTPUT ---
            [Selected system elements to be refined]

            --- STEP 4 OUTPUT ---
            [Multiple design options evaluated, selected option and its advantages]

            --- STEP 5 OUTPUT ---
            [Concrete architecture components, responsibility allocation, interface definitions]

            --- STEP 6a OUTPUT (View Requirements)---
            Generate following types of architecture views using Mermaid (select based on iteration stage):
            - Iteration 1: C1 System Context Diagram + C2 Container Diagram
            - Iteration 2-3: C3 Component Diagram + Key interaction sequence diagram
            - Iteration 4: Deployment Diagram + Monitoring architecture diagram

            Format: ```mermaid
            [Mermaid code]
            ```

            --- STEP 6b OUTPUT (Decision Record Requirements)---
            Record at least 3 key architecture decisions in format:
            Decision ID | Decision Title | Context/Problem | Considered Options | Final Choice | Rationale | Related Quality Attributes

            --- STEP 7 OUTPUT ---
            [Design analysis: Does it meet iteration objectives? Which driving factors are satisfied? What is still needed? Suggested next iteration direction]
            """;

    public static String buildSystemPromptForIteration(int iterationNumber, String iterationObjective,
                                                       String previousContext) {
        return String.format("""
                You are a senior software architect proficient in ADD 3.0 methodology for system architecture design.

                **IMPORTANT: You MUST respond ENTIRELY in English. Do NOT use Chinese or any other language. All output must be in English only.**

                You are performing iteration %d of architecture design for a Hotel Pricing System (HPS).
                This iteration's design objective is: %s

                %s

                %s

                %s

                Now begin this iteration's architecture design. Follow ADD 3.0's 7 steps strictly and use the output format specified above.
                **Output ONLY in English. All text, explanations, and responses must be in English.**
                """,
                iterationNumber,
                iterationObjective,
                HPS_BUSINESS_CONTEXT,
                ADD_3_0_FRAMEWORK,
                previousContext != null && !previousContext.isEmpty() ? previousContext : "This is the first iteration.");
    }

    public static String buildViewGenerationPrompt(String architectureDescription, String viewType) {
        return switch (viewType.toLowerCase()) {
            case "c1 system context diagram" -> buildC1Prompt(architectureDescription);
            case "c2 container diagram" -> buildC2Prompt(architectureDescription);
            case "c3 component diagram" -> buildC3Prompt(architectureDescription);
            case "sequence diagram (price update flow)" -> buildSequenceDiagramPrompt(architectureDescription);
            case "deployment diagram" -> buildDeploymentPrompt(architectureDescription);
            case "monitoring architecture diagram" -> buildMonitoringPrompt(architectureDescription);
            default -> buildGenericMermaidPrompt(architectureDescription, viewType);
        };
    }

    private static String buildC1Prompt(String architectureDescription) {
        return """
                Generate a C1 System Context Diagram based on the following architecture design.
                **Output ONLY valid Mermaid code. Do NOT include any explanatory text.**

                Architecture Description:
                %s

                Requirements:
                1. Show the entire "Hotel Pricing System (HPS)" as the central system
                2. Show key external interaction participants:
                   - Users (administrators, business users)
                   - Cloud identity service provider
                   - Channel Management System (CMS)
                3. Show unidirectional/bidirectional relationships and data flows between system and external entities
                4. Use Mermaid graph syntax with "graph LR" (left to right) layout
                5. Example format:
                ```mermaid
                graph LR
                    User["👤 User"]
                    HPS["🏢 Hotel Pricing System"]
                    IdService["☁️ Cloud Identity Service"]
                    CMS["📊 Channel Management System"]

                    User -->|Login Request| HPS
                    HPS -->|Verify Credentials| IdService
                    HPS -->|Push Prices| CMS
                    CMS -->|Query Status| HPS
                ```

                6. Output ONLY the Mermaid code block (wrapped in ```mermaid ... ```), no other text.

                Generate C1 diagram for this system:
                """.formatted(architectureDescription);
    }

    private static String buildC2Prompt(String architectureDescription) {
        return """
                Generate a C2 Container Diagram based on the following architecture design.
                **Output ONLY valid Mermaid code. Do NOT include any explanatory text.**

                Architecture Description:
                %s

                Requirements:
                1. Show major runtime containers within the system (not code-level components):
                   - HPS Web UI (frontend)
                   - API Gateway
                   - Backend microservices (e.g., Pricing Service, Query Service, etc.)
                   - Message Broker (Kafka)
                   - Data stores (PostgreSQL, Redis, etc.)
                2. Show communication relationships between containers and protocols used (HTTP/REST, Kafka, database drivers)
                3. Use Mermaid graph syntax
                4. Example format:
                ```mermaid
                graph TB
                    subgraph Clients["Clients"]
                        WebUI["🌐 Web UI<br/>Angular"]
                        MobileApp["📱 Mobile App"]
                    end

                    subgraph Backend["Backend Containers"]
                        ApiGw["🚪 API Gateway<br/>Spring Boot"]
                        PricingService["💰 Pricing Service<br/>Java"]
                        QueryService["🔍 Query Service<br/>Java"]
                    end

                    subgraph Data["Data & Messaging"]
                        Kafka["📨 Kafka<br/>Message Broker"]
                        DB["🗄️ PostgreSQL<br/>Database"]
                        Redis["⚡ Redis<br/>Cache"]
                    end

                    WebUI -->|HTTP/REST| ApiGw
                    ApiGw -->|HTTP| PricingService
                    ApiGw -->|HTTP| QueryService
                    PricingService -->|Publish| Kafka
                    Kafka -->|Subscribe| QueryService
                    PricingService -->|SQL| DB
                    QueryService -->|SQL| DB
                    QueryService -->|Cache| Redis
                ```

                5. Output ONLY the Mermaid code block, no other text.

                Generate C2 diagram for this system:
                """.formatted(architectureDescription);
    }

    private static String buildC3Prompt(String architectureDescription) {
        return """
                Generate a C3 Component Diagram based on the following architecture design.

                Architecture Description:
                %s

                Requirements:
                1. Focus on component decomposition within a key container (e.g., Pricing Service or Price Query Service)
                2. Show major components, modules, classes or services within that container
                3. Show dependencies between components
                4. Label each component with its responsibilities (brief description)
                5. Use Mermaid flowchart or graph syntax
                6. Output only Mermaid code block, no other explanatory text

                Generate C3 diagram for this system:
                """.formatted(architectureDescription);
    }

    private static String buildSequenceDiagramPrompt(String architectureDescription) {
        return """
                Generate a Sequence Diagram based on the following architecture design, showing key interactions in the price update flow.

                Architecture Description:
                %s

                Requirements:
                1. Title: Price Update Flow
                2. Participants: User, Web UI, API Gateway, Pricing Service, Kafka, Event Publisher, Channel System
                3. Show the following key steps:
                   a) User submits price change
                   b) System validates permissions and business rules
                   c) Publish PriceChangedEvent to Kafka
                   d) Asynchronous consumers process and calculate derived prices
                   e) Push to channel system
                4. Use Mermaid sequenceDiagram syntax
                5. Output only Mermaid code block, no other explanatory text

                Generate Sequence diagram for this flow:
                """.formatted(architectureDescription);
    }

    private static String buildDeploymentPrompt(String architectureDescription) {
        return """
                Generate a Deployment Diagram based on the following architecture design.

                Architecture Description:
                %s

                Requirements:
                1. Show physical/logical deployment locations of components in production environment:
                   - Different nodes/Pods in Kubernetes cluster
                   - Cloud services (e.g., cloud identity service, cloud storage)
                   - External systems (Channel Management System)
                2. Label each deployment unit with technology used (Docker, K8s, cloud service type)
                3. Show network connections between deployment units
                4. Use Mermaid graph syntax or other diagram syntax
                5. Output only Mermaid code block, no other explanatory text

                Generate Deployment diagram for this system:
                """.formatted(architectureDescription);
    }

    private static String buildMonitoringPrompt(String architectureDescription) {
        return """
                Generate a Monitoring Architecture Diagram based on the following architecture design.

                Architecture Description:
                %s

                Requirements:
                1. Show components of monitoring system:
                   - Prometheus (metrics collection)
                   - Grafana (visualization)
                   - Loki (log aggregation)
                   - Tempo (distributed tracing)
                   - Alertmanager (alerting)
                2. Show data flow: services -> metrics/logs/trace backends -> visualization/alerting
                3. Label each component with its function
                4. Use Mermaid graph syntax
                5. Output only Mermaid code block, no other explanatory text

                Generate Monitoring Architecture diagram for this system:
                """.formatted(architectureDescription);
    }

    private static String buildGenericMermaidPrompt(String architectureDescription, String viewType) {
        return """
                Generate a Mermaid format %s view based on the following architecture design.

                Architecture Design:
                %s

                Requirements:
                1. Generate diagram using Mermaid syntax (recommend graph, flowchart, sequenceDiagram, classDiagram, etc.)
                2. Clearly show system components/containers and relationships between them
                3. Diagram should be meaningful and easy to understand
                4. Output only Mermaid code block (```mermaid ... ```), no other text
                5. Ensure code block is complete and not truncated

                Generate this view:
                """.formatted(viewType, architectureDescription);
    }

    public static String buildDecisionRecordingPrompt(String stepContext, String designAlternatives,
                                                     String chosenDesign) {
        return String.format("""
                Extract at least 3 key architecture decisions based on the following architecture design information.

                Design Context:
                %s

                Design Options Considered:
                %s

                Final Selected Option:
                %s

                List decisions in the following format, one decision per line:
                Decision ID | Decision Title | Context/Problem | Considered Options | Final Choice | Rationale | Related Quality Attributes (comma-separated)
                """,
                stepContext, designAlternatives, chosenDesign);
    }
}
