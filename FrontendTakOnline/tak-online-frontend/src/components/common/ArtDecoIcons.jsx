function CreateRoomIcon({ className = "" }) {
  return (
    <svg
      className={className}
      viewBox="0 0 64 64"
      fill="none"
      aria-hidden="true"
    >
      <path
        d="M20 22H44C46.2091 22 48 23.7909 48 26V46C48 48.2091 46.2091 50 44 50H20C17.7909 50 16 48.2091 16 46V26C16 23.7909 17.7909 22 20 22Z"
        stroke="currentColor"
        strokeWidth="2.2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M32 29V43M25 36H39"
        stroke="currentColor"
        strokeWidth="2.2"
        strokeLinecap="round"
      />
    </svg>
  );
}

function JoinRoomIcon({ className = "" }) {
  return (
    <svg
      className={className}
      viewBox="0 0 64 64"
      fill="none"
      aria-hidden="true"
    >
      <path
        d="M20 20H36C38.2091 20 40 21.7909 40 24V48C40 50.2091 38.2091 52 36 52H20C17.7909 52 16 50.2091 16 48V24C16 21.7909 17.7909 20 20 20Z"
        stroke="currentColor"
        strokeWidth="2.2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M30 36H50M44 30L50 36L44 42"
        stroke="currentColor"
        strokeWidth="2.2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function SunDecoIcon({ className = "" }) {
  return (
    <svg
      className={className}
      viewBox="0 0 64 64"
      fill="none"
      aria-hidden="true"
    >
      <circle cx="32" cy="32" r="10" stroke="currentColor" strokeWidth="2.4" />
      <path
        d="M32 8V16M32 48V56M8 32H16M48 32H56M15 15L21 21M43 43L49 49M49 15L43 21M21 43L15 49"
        stroke="currentColor"
        strokeWidth="2.2"
        strokeLinecap="round"
      />
    </svg>
  );
}

function MoonDecoIcon({ className = "" }) {
  return (
    <svg
      className={className}
      viewBox="0 0 64 64"
      fill="none"
      aria-hidden="true"
    >
      <path
        d="M42 10C34 12 28 19 28 28C28 39 37 48 48 48C50 48 52 47.8 54 47.2C50.8 52.4 44.9 56 38 56C27 56 18 47 18 36C18 24.8 27 16 38.2 16C39.5 16 40.8 16.1 42 16.4V10Z"
        stroke="currentColor"
        strokeWidth="2.4"
        strokeLinejoin="round"
      />
      <path
        d="M14 14L16 18L20 20L16 22L14 26L12 22L8 20L12 18L14 14Z"
        fill="currentColor"
      />
    </svg>
  );
}

export { CreateRoomIcon, JoinRoomIcon, SunDecoIcon, MoonDecoIcon };
