# Spring Boot Starters — Opinionated, Production-Ready Autoconfiguration

A curated set of **custom Spring Boot starters** designed to **accelerate service scaffolding**, **enforce consistent defaults**, and **reduce configuration drift** across Spring-based applications.

This project demonstrates how teams can extend Spring Boot itself—using **auto-configuration, conditional wiring, and sensible defaults**—to build internal platforms that scale.

---

## 🎯 Why Custom Starters?

As Spring-based systems grow, teams commonly face:
- duplicated configuration across services
- inconsistent logging, security, and observability
- boilerplate setup that slows down new services
- configuration drift between environments

**Custom Spring Boot starters solve these problems** by packaging reusable infrastructure concerns into **drop-in dependencies**.

> Add a dependency. Get a working, production-ready setup—by default.

---

## 🧠 What This Repository Demonstrates

This project focuses on **how to design and build starters correctly**, not just that they exist.

It demonstrates:
- Spring Boot auto-configuration patterns
- Conditional bean registration
- Externalized configuration with safe defaults
- Extension points for application-level overrides
- Versioning and backward compatibility considerations

This is **platform and framework engineering**, not application code.

---

## 🧩 How Starters Work (Conceptual Overview)

```text
Application
   |
   |-- spring-boot-starter-logging
   |-- spring-boot-starter-security
   |-- spring-boot-starter-observability
           |
           +-- AutoConfiguration classes
           +-- Conditional beans (@ConditionalOnClass, @ConditionalOnProperty)
           +-- Opinionated defaults
           +-- Optional overrides via application.yml
```

Each starter:

- activates automatically when present on the classpath
- configures infrastructure components safely
- avoids interfering when the application provides its own beans

---

🚀 Usage Example

```gradle
dependencies {
    implementation("org.sandcastle.apps.platform:spring-boot-starter-logging")
    implementation("org.sandcastle.apps.platform:spring-boot-starter-observability")
}
```

With zero configuration, the application starts with:

- structured logging
- consistent log formats
- baseline metrics and tracing
- safe defaults
- Optional overrides are applied via `application.yml`.

---

🧭 Design Principles

- Convention over configuration
- Opinionated defaults with escape hatches
- No surprises on the classpath
- Fail fast when misconfigured
- Backward compatibility within major versions
- Starters should reduce cognitive load, not add magic.

---

🧪 Testing Strategy

- Auto-configuration tests using `@SpringBootTest`
- Conditional wiring tests per starter
- Context-load verification
- Property override validation

Each starter is tested in isolation.

---

📈 Versioning & Compatibility

- Semantic versioning
- Backward-compatible changes within a major version
- Clear upgrade paths
- Release notes documenting behavior changes

This is essential for platform trust.

---

🤝 When to Use (and Not Use) Custom Starters

**Use starters when**:

- many services share the same infrastructure concerns
- consistency and governance matter
- teams want fast, safe service bootstrapping

**Avoid starters when**:

- behavior must vary widely per service
- configuration is experimental or unstable
- the abstraction adds more complexity than value

---

🛣 Roadmap

- [ ] Add reference demo application
- [ ] Expand observability integrations
- [ ] Document extension points per starter
- [ ] Publish first stable release (v1.0.0)
- [ ] Add compatibility matrix per Spring Boot version

Upcoming starters:
- **logging-starter**: Demonstrates auto-configuration, conditional beans, and default overrides.
- **observability-starter**: Strong signal of production readiness and modern platform thinking.
- **security-starter**: Shows governance, consistency, and risk-aware design.
- **rest-client-starter**: Illustrates integration standards and client abstraction.

---

📄 License

MIT License — free to use, extend, and adapt.
