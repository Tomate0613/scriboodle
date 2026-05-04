{
  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs?ref=nixos-unstable";
  };

  outputs =
    { nixpkgs, ... }:

    let
      inherit (nixpkgs) lib;
      systems = lib.systems.flakeExposed;

      forAllSystems = lib.genAttrs systems;

      nixpkgsFor = forAllSystems (system: nixpkgs.legacyPackages.${system});
    in
    {

      packages = forAllSystems (
        system:
        let
          pkgs = nixpkgsFor.${system};
        in
        {
          default = pkgs.stdenv.mkDerivation {
            pname = "xinput-pen";
            version = "0.1.0";

            src = ./.;

            nativeBuildInputs = with pkgs; [ pkg-config ];

            buildInputs = with pkgs; [
              libx11
              libxi
            ];

            buildPhase = ''
              gcc -shared -fPIC pen.c -o libpen.so \
                $(pkg-config --cflags --libs x11 xi)
            '';

            installPhase = ''
              mkdir -p $out/lib
              mkdir -p $out/include

              cp libpen.so $out/lib/
              cp pen.h $out/include/
            '';
          };
        }
      );
      devShells = forAllSystems (
        system:
        let
          pkgs = nixpkgsFor.${system};
        in
        {
          default = pkgs.mkShell {
            buildInputs = with pkgs; [
              gcc
              pkg-config
              libx11
              libxi
            ];
          };
        }
      );
    };
}
