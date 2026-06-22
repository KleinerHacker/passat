<div class="passat-hero" markdown>
![Passat](assets/images/icon.png){ alt="Passat" }
</div>

# Passat

<p class="passat-tagline">IntelliJ 플랫폼 위에 구축된 오픈 플랫폼 Object Pascal IDE.</p>

Passat 는 IntelliJ 플랫폼에 최고 수준의 **Object Pascal** 지원을 제공합니다. **무료 Object Pascal
컴파일러** 위에 구축되었으며, **Free Pascal 컴파일러(FPC)**를 기준 툴체인으로 사용합니다. 컴파일러와
언어 버전 처리는 추상화되어 있어, 이후 다른 무료 Object Pascal 컴파일러도 추가할 수 있습니다.

Passat 는 **플러그인 우선** 방식으로 개발됩니다. 기존 JetBrains IDE 에 설치할 수 있는 플러그인으로
제공되며, 이후 단계에서는 동일한 코드베이스로부터 **독립 실행형 IDE** 배포본도 패키징됩니다.

## 기능

- **전용 Pascal 프로젝트 및 모듈 타입** — Java 및 Kotlin 모듈처럼, 각 Pascal 모듈은 대상 컴파일러
  (SDK 로 등록된 FPC 설치), 대상 언어 버전(Object Pascal 방언 수준), 그리고 자체 의존성 목록을 가집니다.
- **완전한 언어 지원** — PSI 트리를 생성하는 렉서와 파서, 구문 강조, 코드 자동 완성, 탐색(선언으로 이동 /
  사용처 찾기), 구조 보기, 리팩터링 및 인스펙션.
- **빌드 및 실행** — 전용 실행 구성을 통해 구성된 컴파일러로 프로젝트를 컴파일하고 실행합니다.
- **디버깅** — 중단점, 단계 실행, 변수 검사, 표현식 평가 등 완전한 IDE 디버깅 기능.
- **이중 배포** — 하나의 코드베이스, 두 가지 제품: 기존 JetBrains IDE 용 설치형 플러그인**과** 독립 실행형
  Passat IDE.

## 로드맵

Passat 는 단계적으로 구축됩니다:

1. **언어 파서 및 PSI** — 렉서, 문법, PSI, 기본 강조.
2. **프로젝트 / 모듈 모델** — 컴파일러 SDK, 언어 버전, 의존성을 갖춘 Pascal 모듈 타입.
3. **빌드 및 실행** — FPC 를 통한 컴파일 및 실행, 실행 구성.
4. **디버깅** — 완전한 디버그 기능 집합.
5. **독립 실행형 IDE 패키징** — 플러그인에 더해 Passat 를 자체 IDE 로 배포.
