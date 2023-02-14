
import resolve from '@rollup/plugin-node-resolve';
import commonjs from '@rollup/plugin-commonjs';
import typescript from 'rollup-plugin-typescript2';
import progress from 'rollup-plugin-progress';

export default [
    {
        input: 'src/www/api.ts',
        external: [
            'cordova'
        ],
        output: [
            {
                file: 'www/insets.js',
                format: 'cjs',
                sourcemap: true
            }
        ],
        plugins: [
            resolve({
                preferBuiltins: true
            }),
            typescript(),
            // Order matters, most plugins needs to be above commonjs
            commonjs(),
            progress()
        ]
    }
];
